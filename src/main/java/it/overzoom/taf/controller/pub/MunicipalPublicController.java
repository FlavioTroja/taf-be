package it.overzoom.taf.controller.pub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.overzoom.taf.dto.MunicipalDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.MunicipalMapper;
import it.overzoom.taf.model.Municipal;
import it.overzoom.taf.service.MunicipalService;

@RestController
@RequestMapping("/api/public/municipals")
public class MunicipalPublicController {

    private static final Logger log = LoggerFactory.getLogger(MunicipalPublicController.class);
    private final MunicipalService municipalService;
    private final MunicipalMapper municipalMapper;

    public MunicipalPublicController(MunicipalService municipalService, MunicipalMapper municipalMapper) {
        this.municipalService = municipalService;
        this.municipalMapper = municipalMapper;
    }

    @GetMapping("/{domain}")
    @Operation(summary = "Recupera il comune per dominio", description = "Restituisce i dettagli di un comune basato sul dominio specificato", parameters = {
            @Parameter(name = "domain", description = "Dominio del comune da recuperare", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Comune trovato con successo"),
            @ApiResponse(responseCode = "404", description = "Comune non trovato con il dominio fornito")
    })
    public ResponseEntity<MunicipalDTO> getMunicipalByDomain(@PathVariable String domain)
            throws ResourceNotFoundException {
        log.info("REST request to get Municipal by domain: {}", domain);
        Municipal municipal = municipalService.findByDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("Municipal not found with domain: " + domain));
        return ResponseEntity.ok().body(municipalMapper.toDto(municipal));
    }
}
