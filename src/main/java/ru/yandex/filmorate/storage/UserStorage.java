package ru.yandex.filmorate.storage;

import ru.yandex.filmorate.model.User;

import java.io.IOException;
import java.util.Map;

public interface UserStorage {

    User addUser(User user);
    User getUser(Long id);
    User deleteUser(Long id);
    User updateUser(User user) throws IOException;
    Map<Long, User> getUsers();
    void clean();

}
