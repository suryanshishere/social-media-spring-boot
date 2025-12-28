package com.cool.socialmedia.social_media.users;

import java.time.LocalDate;
import java.time.Period;

/**
 * Version 2 User DTO with enhanced fields
 * Wraps the base User entity with additional computed fields
 */
public class UserV2 {

    private Integer id;
    private String name;
    private LocalDate dob;

    // V2 enhanced fields
    private String fullName;
    private Integer age;

    public UserV2() {
    }

    public UserV2(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.dob = user.getDob();

        // Compute enhanced fields
        this.fullName = user.getName() != null ? "Mr./Ms. " + user.getName() : null;
        this.age = calculateAge(user.getDob());
    }

    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // Getters and Setters
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
