package ru.yandex.filmorate.storage;

import ru.yandex.filmorate.model.User;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);
    Optional<User> getUser(Long id);
    User deleteUser(Long id);
    User updateUser(User user) throws IOException;
    Map<Long, User> getUsers();
    void clean();

}
