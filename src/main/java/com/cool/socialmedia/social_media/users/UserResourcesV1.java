package com.cool.socialmedia.social_media.users;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
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

/**
 * REST API Version 1 - Basic user data
 * 
 * Versioning Strategies Demonstrated:
 * 1. URI Path Versioning: /v1/users (this controller)
 * 2. Request Parameter: /users?version=1 (see param endpoints)
 * 3. Header Versioning: X-API-VERSION: 1 (see header endpoints)
 * 4. Media Type: Accept: application/vnd.socialmedia.app-v1+json (see produces
 * endpoints)
 */
@RestController
@Validated
@RequestMapping("/v1")
public class UserResourcesV1 {

    private final UserDaoService userDaoService;
    private final MessageSource messageSource;

    public UserResourcesV1(UserDaoService userDaoService, MessageSource messageSource) {
        this.userDaoService = userDaoService;
        this.messageSource = messageSource;
    }

    // ==================== URI PATH VERSIONING ====================
    // Access via: GET /v1/users/{id}

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Integer id) {
        return userDaoService.findOne(id);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userDaoService.findAll();
    }

    @PostMapping("/users")
    public org.springframework.http.ResponseEntity<Object> createUser(@Valid @RequestBody User user) {
        Locale locale = LocaleContextHolder.getLocale();

        if (user.getId() != null && userDaoService.findOne(user.getId()) != null) {
            String errorMessage = messageSource.getMessage("user.already.exists", new Object[] { user.getId() },
                    locale);
            throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
        }
        User savedUser = userDaoService.save(user);

        java.net.URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("message", messageSource.getMessage("user.created.success", null, locale));
        response.put("data", savedUser);

        return org.springframework.http.ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/users/{id}")
    public org.springframework.http.ResponseEntity<Object> deleteUser(
            @PathVariable @Positive(message = "ID must be positive") Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        User user = userDaoService.findOne(id);
        if (user == null) {
            String errorMessage = messageSource.getMessage("user.not.found", new Object[] { id }, locale);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);
        }
        userDaoService.delete(id);

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("message", messageSource.getMessage("user.deleted.success", null, locale));

        return org.springframework.http.ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // ==================== REQUEST PARAM VERSIONING ====================
    // Access via: GET /users/param/{id}?version=1

    @GetMapping(value = "/users/param/{id}", params = "version=1")
    public User getUserByParam(@PathVariable Integer id) {
        return userDaoService.findOne(id);
    }

    // ==================== HEADER VERSIONING ====================
    // Access via: GET /users/header/{id} with header X-API-VERSION: 1

    @GetMapping(value = "/users/header/{id}", headers = "X-API-VERSION=1")
    public User getUserByHeader(@PathVariable Integer id) {
        return userDaoService.findOne(id);
    }

    // ==================== MEDIA TYPE (CONTENT NEGOTIATION) VERSIONING
    // ====================
    // Access via: GET /users/accept/{id} with header Accept:
    // application/vnd.socialmedia.app-v1+json

    @GetMapping(value = "/users/accept/{id}", produces = "application/vnd.socialmedia.app-v1+json")
    public User getUserByMediaType(@PathVariable Integer id) {
        return userDaoService.findOne(id);
    }
}
