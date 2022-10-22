package ru.yandex.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.User;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage{

    private Long idUser = 1L;
    private final Map<Long, User> users = new HashMap<>();

    public Map<Long, User> getUsers() {
        return users;
    }

    @Override
    public User getUser(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден!");
        }
        return users.get(id);
    }

    @Override
    public void clean() {
        users.clear();
        idUser = 1L;
    }

    @Override
    public User addUser(User user) {
        log.debug("Создание пользователя");
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

    @Override
    public User updateUser(User user) {
        log.debug("Обновление пользователя");
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден!");
        }
        users.put(user.getId(), user);
        log.info("Обновлен пользователь: id = {}, login = {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User deleteUser(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден!");
        }
        return users.remove(id);
    }
}
