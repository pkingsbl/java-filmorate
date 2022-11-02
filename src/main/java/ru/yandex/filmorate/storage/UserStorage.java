package ru.yandex.filmorate.storage;

import ru.yandex.filmorate.model.User;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);
    Optional<User> getUser(Long id);
    void deleteUser(Long id);
    User updateUser(User user) throws IOException;
    Map<Long, User> getUsers();

    void addFriend(Long id, Long friendId);

    void deleteFriend(Long id, Long friendId);

    List<User> findFriends(Long id);

    List<User> getMutualFriends(Long id, Long otherId);

}
