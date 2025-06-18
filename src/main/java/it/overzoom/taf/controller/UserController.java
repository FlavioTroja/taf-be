package it.overzoom.taf.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.dto.UserDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.UserMapper;
import it.overzoom.taf.model.User;
import it.overzoom.taf.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseSearchController<User, UserDTO> {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    protected String getCollectionName() {
        return "user";
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected Function<User, UserDTO> toDtoMapper() {
        return userMapper::toDto;
    }

    @GetMapping("")
    public ResponseEntity<Page<UserDTO>> findAll(
            Pageable pageable) {
        log.info("REST request to get a page of Users");
        Page<User> page = userService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(userMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable("id") String userId)
            throws ResourceNotFoundException, BadRequestException {

        if (!userService.hasAccess(userId)) {
            throw new BadRequestException("Non hai i permessi per accedere a questo utente.");
        }

        return userService.findById(userId).map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato."));
    }

    @PostMapping("/create")
    public ResponseEntity<User> create(@Valid @RequestBody UserDTO userDTO)
            throws BadRequestException, URISyntaxException {
        log.info("REST request to save User : " + userDTO.toString());
        if (userDTO.getId() != null) {
            throw new BadRequestException("Un nuovo cliente non può già avere un ID");
        }
        User user = userMapper.toEntity(userDTO);
        user = userService.create(user);
        return ResponseEntity.created(new URI("/api/users/" + userDTO.getId())).body(user);
    }

    @PutMapping("")
    public ResponseEntity<UserDTO> update(@Valid @RequestBody UserDTO userDTO) throws BadRequestException,
            ResourceNotFoundException {
        log.info("REST request to update User:" + userDTO.toString());
        if (userDTO.getId() == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!userService.existsById(userDTO.getId())) {
            throw new ResourceNotFoundException("Utente non trovato.");
        }
        User user = userMapper.toEntity(userDTO);
        User updateUser = userService.update(user).orElseThrow(
                () -> new ResourceNotFoundException("Utente non trovato con questo ID :: " + user.getId()));

        return ResponseEntity.ok().body(userMapper.toDto(updateUser));
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<UserDTO> partialUpdate(@PathVariable("id") String id,
            @RequestBody UserDTO userDTO) throws BadRequestException,
            ResourceNotFoundException {
        log.info("REST request to partial update User: " + userDTO.toString());
        if (id == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!userService.existsById(id)) {
            throw new ResourceNotFoundException("Cliente non trovato.");
        }
        User user = userMapper.toEntity(userDTO);
        User updateUser = userService.partialUpdate(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con questo ID :: " + id));

        return ResponseEntity.ok().body(userMapper.toDto(updateUser));
    }

    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<UserDTO> uploadUserPhoto(@PathVariable("id") String id,
            @RequestParam("file") MultipartFile file) throws ResourceNotFoundException, IOException {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con ID :: " + id));

        // Verifica se il file è vuoto
        if (file.isEmpty()) {
            throw new RuntimeException("Nessun file caricato");
        }

        // Estrai l'estensione del file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("Nome file non valido o mancante estensione");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);

        // Crea il nome del file con timestamp
        String fileName = System.currentTimeMillis() + "." + extension;

        // Definisci la cartella di destinazione
        Path path = Paths.get("uploads/photos", fileName);

        try {
            // Salva il file fisicamente nella cartella
            Files.createDirectories(path.getParent()); // Assicurati che la cartella esista
            file.transferTo(path.toFile());

            // Salva solo il nome del file nel database
            user.setPhoto(fileName);
            userService.create(user);

            // Restituisci il DTO dell'utente
            return ResponseEntity.ok(userMapper.toDto(user));

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento del file", e);
        }
    }

}
