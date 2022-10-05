package ru.yandex.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.User;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private int idUser = 1;

    private final Map<Integer, User> users = new HashMap<>();

    public void clean(){
        this.users.clear();
        this.idUser = 1;
    }
    @PostMapping
    public User create(@Valid @RequestBody User user) throws ValidationException {
        log.debug("POST /users создание пользователя");
        if (user.getName() == null || user.getName().isEmpty()){
            user.setName(user.getLogin());
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин содержит пробелы");
        }
        user.setId(idUser++);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: id = {}, login = {}", user.getId(), user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) throws IOException {
        log.debug("PUT /users обновление пользователя");
        if (!users.containsKey(user.getId())) {
            throw new IOException("Пользователь с id = " + user.getId() + " не найден!");
        }
        users.put(user.getId(), user);
        log.info("Обновлен пользователь: id = {}, login = {}", user.getId(), user.getLogin());
        return user;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.debug("GET /users получение списка всех пользователей");
        log.info("Текущее количество пользователей: {}", users.size());
        return users.values();
    }

}
