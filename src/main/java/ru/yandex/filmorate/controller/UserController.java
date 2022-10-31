package ru.yandex.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.User;
import ru.yandex.filmorate.service.UserService;
import ru.yandex.filmorate.storage.UserStorage;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@Configurable
@RequestMapping("/users")
public class UserController {

    @Qualifier("UserDbStorage")
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private UserService userService;

    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException {
        return userStorage.addUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) throws IOException {
        return userStorage.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable @Min(value = 1, message = "id должен быть больше 0") Long id,
            @PathVariable Long friendId) {
        userStorage.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable @Min(value = 1, message = "id должен быть больше 0") Long id,
            @PathVariable @Min(value = 1, message = "id должен быть больше 0") Long friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable @Min(value = 1, message = "id должен быть больше 0") Long id) {
        userStorage.deleteUser(id);
    }

    @GetMapping
    public Collection<User> findAll() {
        return new ArrayList<>(userStorage.getUsers().values());
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable @Min(value = 1, message = "id должен быть больше 0") Long id) {
        if (userStorage.getUser(id).isPresent()) {
            return userStorage.getUser(id).get();
        }
        log.info("Пользователь с идентификатором {} не найден.", id);
        throw new NotFoundException("Пользователь с id = " + id + " не найден!");
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable @Min(value = 1, message = "id должен быть больше 0") Long id) {
        return userStorage.findFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> findCommonFriends(@PathVariable @Min(value = 1, message = "id должен быть больше 0") Long id,
            @PathVariable @Min(value = 1, message = "user id должен быть больше 0") Long otherId)  {
        return userStorage.getMutualFriends(id, otherId);
    }

}
