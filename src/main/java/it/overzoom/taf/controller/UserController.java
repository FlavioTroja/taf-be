package it.overzoom.taf.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Override
    protected List<String> getSearchableFields() {
        return List.of("name", "surname", "userId", "municipalityId");
    }

    @GetMapping("")
    public ResponseEntity<Page<UserDTO>> findAll(
            Pageable pageable) {
        log.info("REST request to get a page of Users");
        Page<User> page = userService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(userMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable("id") String id)
            throws ResourceNotFoundException, BadRequestException {

        // if (!userService.hasAccess(id)) {
        // throw new BadRequestException("Non hai i permessi per accedere a questo
        // utente.");
        // }

        return userService.findById(id).map(userMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato."));
    }

    @PostMapping("/create")
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserDTO userDTO)
            throws BadRequestException, URISyntaxException {
        log.info("REST request to save User : " + userDTO.toString());
        if (userDTO.getId() != null) {
            throw new BadRequestException("Un nuovo cliente non può già avere un ID");
        }
        User user = userMapper.toEntity(userDTO);
        user = userService.create(user);
        return ResponseEntity.created(new URI("/api/users/" + user.getId())).body(userMapper.toDto(user));
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

        if (file.isEmpty()) {
            throw new BadRequestException("Nessun file caricato.");
        }

        User user = userService.uploadPhoto(id, file);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete User with ID: {}", id);
        if (!userService.existsById(id)) {
            throw new ResourceNotFoundException("Utente non trovato.");
        }
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
