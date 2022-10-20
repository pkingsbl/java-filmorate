package ru.yandex.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.User;
import ru.yandex.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserStorage userStorage;

    public User addFriend(Long firstId, Long secondId) {
        if (firstId.longValue() == secondId.longValue()) {
            throw new ValidationException("Id пользователей совпадает");
        }
        if (userStorage.getUsers().containsKey(firstId) && userStorage.getUsers().containsKey(secondId)) {

            userStorage.getUsers().get(firstId).getFriends().add(secondId);
            userStorage.getUsers().get(secondId).getFriends().add(firstId);
            log.info("Пользователи " + userStorage.getUsers().get(firstId).getLogin() + " и "
                    + userStorage.getUsers().get(secondId).getLogin() + " друзьяшки на веки");

            return userStorage.getUsers().get(secondId);
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public User deleteFriend(Long firstId, Long secondId) {
        if (firstId.longValue() == secondId.longValue()) {
            throw new ValidationException("Id пользователей совпадает");
        }
        if (userStorage.getUsers().containsKey(firstId) && userStorage.getUsers().containsKey(secondId)) {

            userStorage.getUsers().get(firstId).getFriends().remove(secondId);
            userStorage.getUsers().get(secondId).getFriends().remove(firstId);
            log.info("Пользователи " + userStorage.getUsers().get(firstId).getLogin() + " и "
                    + userStorage.getUsers().get(secondId).getLogin() + " больше не друзьяшки");

            return userStorage.getUsers().get(secondId);
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public List<User> getMutualFriends(Long firstId, Long secondId) {
        if (firstId.longValue() == secondId.longValue()) {
            throw new ValidationException("Id пользователей совпадает");
        }
        if (userStorage.getUsers().containsKey(firstId) && userStorage.getUsers().containsKey(secondId)) {

            Set<Long> friends = userStorage.getUsers().get(firstId).getFriends();
            friends.retainAll(userStorage.getUsers().get(secondId).getFriends());

            List<User> mutualFriends = findFriends(friends);
            log.info("Количестко общих друзей: " + mutualFriends.size());
            return mutualFriends;
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public List<User> findFriends(Set<Long> friends) {
        List<User> mutualFriends = new ArrayList<>();

        friends.forEach(it -> mutualFriends.add(userStorage.getUsers().get(it)));
        return mutualFriends;
    }
}

