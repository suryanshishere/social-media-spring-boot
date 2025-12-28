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
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
public class UserResources {

    private final UserDaoService userDaoService;
    private final MessageSource messageSource;

    public UserResources(UserDaoService userDaoService, MessageSource messageSource) {
        this.userDaoService = userDaoService;
        this.messageSource = messageSource;
    }

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

}
