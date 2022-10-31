package ru.yandex.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.User;
import ru.yandex.filmorate.service.UserService;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage{

    @Autowired
    private UserService userService;
    private Long idUser = 1L;
    private final Map<Long, User> users = new HashMap<>();

    public Map<Long, User> getUsers() {
        return users;
    }

    @Override
    public Optional<User> getUser(Long id) {
        if (users.containsKey(id)) {
            return Optional.of(users.get(id));
        }
        return Optional.empty();
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        userService.addFriend(id, friendId);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        userService.deleteFriend(id, friendId);
    }

    @Override
    public List<User> findFriends(Long id) {
        List<User> friends = new ArrayList<>();
        for (Long friendId : users.get(id).getFriends()) {
            friends.add(users.get(friendId));
        }
        return friends;
    }

    @Override
    public List<User> getMutualFriends(Long id, Long otherId) {
        return new ArrayList<>(userService.getMutualFriends(id, otherId));
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
    public void deleteUser(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден!");
        }
        users.remove(id);
    }
}
