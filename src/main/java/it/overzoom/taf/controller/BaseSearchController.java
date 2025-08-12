package it.overzoom.taf.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.overzoom.taf.model.Event;
import it.overzoom.taf.model.News;

public abstract class BaseSearchController<T, DTO> {

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected abstract String getCollectionName();

    protected abstract Class<T> getEntityClass();

    protected abstract Function<T, DTO> toDtoMapper();

    protected List<String> getSearchableFields() {
        return List.of();
    }

    protected List<Criteria> getExtraCriteriaForCurrentUser(Map<String, Object> request) {
        return List.of();
    }

    @PostMapping("/search")
    @Operation(summary = "Esegui una ricerca", description = "Questo endpoint consente di effettuare una ricerca con filtri, ordinamento e ricerca full-text.", parameters = {
            @Parameter(name = "request", description = "Oggetto di ricerca contenente filtri, parametri di ordinamento e termini di ricerca", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Ricerca completata con successo e restituita una pagina di risultati"),
            @ApiResponse(responseCode = "400", description = "Errore nella richiesta, come un parametro mancante o errato")
    })
    public ResponseEntity<Page<DTO>> search(@RequestBody Map<String, Object> request) {
        int page = (int) request.getOrDefault("page", 0);
        int limit = (int) request.getOrDefault("limit", 10);

        Map<String, Object> filters = extractMapObject(request.get("filters"));
        Map<String, String> sortMap = extractMap(request.get("sort"));

        // Lista di criteri (AND)
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.addAll(getExtraCriteriaForCurrentUser(request));

        // Bounding box: filtra per coordinate se presenti
        Map<String, Object> bbox = extractMapObject(request.get("bbox"));
        if (bbox != null) {
            Double north = getDouble(bbox.get("north"));
            Double south = getDouble(bbox.get("south"));
            Double east = getDouble(bbox.get("east"));
            Double west = getDouble(bbox.get("west"));
            if (north != null && south != null && east != null && west != null) {
                andCriteria.add(Criteria.where("latitude").gte(south).lte(north));
                andCriteria.add(Criteria.where("longitude").gte(west).lte(east));
            }
        }

        // Applica filtri normali (e &&)
        filters.forEach((key, value) -> {
            if ("municipalityIds".equals(key))
                return;
            if (value == null)
                return;

            // Gestione array/lista per type e tags
            if (value instanceof List<?> listValue) {
                if (!listValue.isEmpty()) {
                    // Speciale: se il campo è "tags" usa .in() che su array Mongo vuol dire "almeno
                    // uno presente"
                    andCriteria.add(Criteria.where(key).in(listValue));
                }
                return;
            }

            // Gestione stringa
            if (value instanceof String strValue && !strValue.isEmpty()) {
                andCriteria.add(
                        Criteria.where(key).regex(Pattern.compile(Pattern.quote(strValue), Pattern.CASE_INSENSITIVE)));
            }
        });

        // --- Logica search full-text (OR tra i campi, AND con i filtri normali) ---
        String searchText = (String) request.get("search");
        List<String> searchFields = getSearchableFields();
        if (searchText != null && !searchText.isEmpty() && !searchFields.isEmpty()) {
            List<Criteria> orList = searchFields.stream()
                    .map(field -> Criteria.where(field)
                            .regex(Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)))
                    .toList();
            andCriteria.add(new Criteria().orOperator(orList.toArray(new Criteria[0])));
        }

        Criteria criteria;
        if (andCriteria.isEmpty()) {
            criteria = new Criteria();
        } else if (andCriteria.size() == 1) {
            criteria = andCriteria.get(0);
        } else {
            criteria = new Criteria().andOperator(andCriteria.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        Sort sort = buildSort(sortMap);
        if (sort != null) {
            query.with(sort);
        }
        Pageable pageable = PageRequest.of(page, limit);
        query.with(pageable);

        long total = mongoTemplate.count(query, getEntityClass(), getCollectionName());
        List<T> result = mongoTemplate.find(query, getEntityClass(), getCollectionName());
        List<DTO> dtoList = result.stream().map(toDtoMapper()).toList();

        Page<DTO> pageResult = new PageImpl<>(dtoList, pageable, total);
        return ResponseEntity.ok(pageResult);
    }

    private Map<String, String> extractMap(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                    .collect(Collectors.toMap(
                            e -> (String) e.getKey(),
                            e -> (String) e.getValue()));
        }
        return Map.of();
    }

    // Per estrarre Map<String, Object> in modo sicuro
    private Map<String, Object> extractMapObject(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .filter(e -> e.getKey() instanceof String)
                    .collect(Collectors.toMap(
                            e -> (String) e.getKey(),
                            Map.Entry::getValue));
        }
        return null;
    }

    // Per gestire Double/null robustamente
    private Double getDouble(Object obj) {
        if (obj instanceof Number n)
            return n.doubleValue();
        if (obj instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    protected Sort buildSort(Map<String, String> sortMap) {
        if (sortMap == null || sortMap.isEmpty()) {
            // Imposta l'ordinamento di default
            if (getEntityClass() == Event.class) {
                return Sort.by(Sort.Order.asc("startDateTime")); // Ordinamento per data di inizio evento
            } else if (getEntityClass() == News.class) {
                return Sort.by(Sort.Order.desc("publicationDate")); // Ordinamento per data di creazione
            }
            return null;
        }

        List<Sort.Order> orders = sortMap.entrySet().stream()
                .map(entry -> new Sort.Order(
                        "desc".equalsIgnoreCase(entry.getValue()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                        entry.getKey()))
                .toList();
        return Sort.by(orders);
    }

}
