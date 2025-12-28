package com.cool.socialmedia.social_media.users;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class UserDaoService {

    private static List<User> users = new ArrayList<>();

    static {
        users.add(new User(1, "John", LocalDate.of(1990, 1, 1)));
        users.add(new User(2, "Jane", LocalDate.of(1991, 2, 2)));
        users.add(new User(3, "Bob", LocalDate.of(1992, 3, 3)));
    }

    public List<User> findAll() {
        return users;
    }

    public User save(User user) {
        users.add(user);
        return user;
    }

    public User findOne(Integer id) {
        return users.stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
    }

    public void delete(Integer id) {
        users.removeIf(user -> user.getId().equals(id));
    }

}
