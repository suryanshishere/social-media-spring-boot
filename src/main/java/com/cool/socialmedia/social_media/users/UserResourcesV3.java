package com.cool.socialmedia.social_media.users;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
// import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST API Version 3 - HATEOAS with HAL (Hypertext Application Language)
 * 
 * Returns responses with hypermedia links for discoverability:
 * - _links.self: Link to the current resource
 * - _links.all-users: Link to get all users
 * - _embedded: Embedded resources in collection responses
 */
@RestController
@Validated
@RequestMapping("/v3")
public class UserResourcesV3 {

    private final UserDaoService userDaoService;
    private final MessageSource messageSource;

    public UserResourcesV3(UserDaoService userDaoService, MessageSource messageSource) {
        this.userDaoService = userDaoService;
        this.messageSource = messageSource;
    }

    /**
     * GET /v3/users/{id}
     * Returns a single user wrapped in EntityModel with HATEOAS links
     */
    @GetMapping("/users/{id}")
    public EntityModel<User> getUser(@PathVariable Integer id) {
        User user = userDaoService.findOne(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    messageSource.getMessage("user.not.found", new Object[] { id }, LocaleContextHolder.getLocale()));
        }

        // Create EntityModel with the user and add HATEOAS links
        EntityModel<User> entityModel = EntityModel.of(user);

        // Add self link
        entityModel.add(linkTo(methodOn(this.getClass()).getUser(id)).withSelfRel());

        // Add link to all users
        entityModel.add(linkTo(methodOn(this.getClass()).getAllUsers()).withRel("all-users"));

        return entityModel;
    }

    /**
     * GET /v3/users
     * Returns all users wrapped in CollectionModel with HATEOAS links
     */
    @GetMapping("/users")
    public CollectionModel<EntityModel<User>> getAllUsers() {
        List<EntityModel<User>> users = userDaoService.findAll().stream()
                .map(user -> {
                    EntityModel<User> entityModel = EntityModel.of(user);
                    // Add self link for each user
                    entityModel.add(linkTo(methodOn(this.getClass()).getUser(user.getId())).withSelfRel());
                    return entityModel;
                })
                .collect(Collectors.toList());

        // Wrap in CollectionModel and add self link
        CollectionModel<EntityModel<User>> collectionModel = CollectionModel.of(users);
        collectionModel.add(linkTo(methodOn(this.getClass()).getAllUsers()).withSelfRel());

        return collectionModel;
    }

    /**
     * POST /v3/users
     * Creates a new user and returns it with HATEOAS links
     */
    @PostMapping("/users")
    public ResponseEntity<EntityModel<User>> createUser(@Valid @RequestBody User user) {
        Locale locale = LocaleContextHolder.getLocale();

        if (user.getId() != null && userDaoService.findOne(user.getId()) != null) {
            String errorMessage = messageSource.getMessage("user.already.exists", new Object[] { user.getId() },
                    locale);
            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
        }

        User savedUser = userDaoService.save(user);

        // Build location URI
        java.net.URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        // Create EntityModel with HATEOAS links
        EntityModel<User> entityModel = EntityModel.of(savedUser);
        entityModel.add(linkTo(methodOn(this.getClass()).getUser(savedUser.getId())).withSelfRel());
        entityModel.add(linkTo(methodOn(this.getClass()).getAllUsers()).withRel("all-users"));

        return ResponseEntity.created(location).body(entityModel);
    }

    /**
     * DELETE /v3/users/{id}
     * Deletes a user and returns a response with link to all users
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<EntityModel<Object>> deleteUser(
            @PathVariable @Positive(message = "ID must be positive") Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        User user = userDaoService.findOne(id);
        if (user == null) {
            String errorMessage = messageSource.getMessage("user.not.found", new Object[] { id }, locale);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);
        }

        userDaoService.delete(id);

        // Create response with message and link to remaining users
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("message", messageSource.getMessage("user.deleted.success", null, locale));

        EntityModel<Object> entityModel = EntityModel.of(response);
        entityModel.add(linkTo(methodOn(this.getClass()).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(entityModel);
    }
}
