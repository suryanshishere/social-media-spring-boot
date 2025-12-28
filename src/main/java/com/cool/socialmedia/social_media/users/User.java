package com.cool.socialmedia.social_media.users;

import java.time.LocalDate;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class User {
    @Positive(message = "ID must be positive")
    private Integer id;

    @Size(min = 2, message = "Name should have at least 2 characters")
    private String name;

    @Past(message = "Birth Date should be in the past")
    private LocalDate dob;

    public User() {
    }

    public User(Integer id, String name, LocalDate dob) {
        this.id = id;
        this.name = name;
        this.dob = dob;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
}
