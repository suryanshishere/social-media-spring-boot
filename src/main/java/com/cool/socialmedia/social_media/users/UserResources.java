package com.cool.socialmedia.social_media.users;

import java.util.List;

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

    public UserResources(UserDaoService userDaoService) {
        this.userDaoService = userDaoService;
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
        if (user.getId() != null && userDaoService.findOne(user.getId()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "User with id " + user.getId() + " already exists");
        }
        User savedUser = userDaoService.save(user);

        // Verbosity: Java (Spring) requires more explicit steps to build the URI for
        // the Location header, whereas in Node.js you often just return the JSON.
        java.net.URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("message", "User Created Successfully");
        response.put("data", savedUser);

        return org.springframework.http.ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/users/{id}")
    public org.springframework.http.ResponseEntity<Object> deleteUser(
            @PathVariable @Positive(message = "ID must be positive") Integer id) {
        User user = userDaoService.findOne(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + id + " not found");
        }
        userDaoService.delete(id);

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("message", "User Deleted Successfully");

        return org.springframework.http.ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
