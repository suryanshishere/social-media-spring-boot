package com.cool.socialmedia.social_media.users;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
 * REST API Version 2 - Enhanced user data with additional fields
 * 
 * V2 returns UserV2 which includes additional metadata like fullName and age
 * calculation
 */
@RestController
@Validated
@RequestMapping("/v2")
public class UserResourcesV2 {

    private final UserDaoService userDaoService;
    private final MessageSource messageSource;

    public UserResourcesV2(UserDaoService userDaoService, MessageSource messageSource) {
        this.userDaoService = userDaoService;
        this.messageSource = messageSource;
    }

    // ==================== URI PATH VERSIONING ====================
    // Access via: GET /v2/users/{id}

    @GetMapping("/users/{id}")
    public UserV2 getUser(@PathVariable Integer id) {
        User user = userDaoService.findOne(id);
        if (user == null) {
            return null;
        }
        return new UserV2(user);
    }

    @GetMapping("/users")
    public List<UserV2> getAllUsers() {
        return userDaoService.findAll().stream()
                .map(UserV2::new)
                .collect(Collectors.toList());
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
        response.put("data", new UserV2(savedUser));
        response.put("apiVersion", "v2");

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
        response.put("apiVersion", "v2");

        return org.springframework.http.ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // ==================== REQUEST PARAM VERSIONING ====================
    // Access via: GET /users/param/{id}?version=2

    @GetMapping(value = "/users/param/{id}", params = "version=2")
    public UserV2 getUserByParam(@PathVariable Integer id) {
        User user = userDaoService.findOne(id);
        return user != null ? new UserV2(user) : null;
    }

    // ==================== HEADER VERSIONING ====================
    // Access via: GET /users/header/{id} with header X-API-VERSION: 2

    @GetMapping(value = "/users/header/{id}", headers = "X-API-VERSION=2")
    public UserV2 getUserByHeader(@PathVariable Integer id) {
        User user = userDaoService.findOne(id);
        return user != null ? new UserV2(user) : null;
    }

    // ==================== MEDIA TYPE (CONTENT NEGOTIATION) VERSIONING
    // ====================
    // Access via: GET /users/accept/{id} with header Accept:
    // application/vnd.socialmedia.app-v2+json

    @GetMapping(value = "/users/accept/{id}", produces = "application/vnd.socialmedia.app-v2+json")
    public UserV2 getUserByMediaType(@PathVariable Integer id) {
        User user = userDaoService.findOne(id);
        return user != null ? new UserV2(user) : null;
    }
}
