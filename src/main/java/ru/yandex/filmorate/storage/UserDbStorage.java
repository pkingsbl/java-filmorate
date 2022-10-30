package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.model.User;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    @Override
    public User addUser(User user) {
        return null;
    }

    @Override
    public Optional<User> getUser(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from films where id = ?", id);

        if(userRows.next()) {
            User user = new User(
                    userRows.getLong("id"),
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("name"),
                    Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate());
            String sql = "SELECT f.friend_id FROM friends AS f WHERE f.user_id = ? AND f.APPROVE = true";
            SqlRowSet friendsRows = jdbcTemplate.queryForRowSet(sql, id);
            while (friendsRows.next()) {
                user.getFriends().add(friendsRows.getLong("friend_id"));
            }
            log.info("Найден пользователь: {}", user.getLogin());
            return Optional.of(user);
        } else {
            log.info("Пользователь с идентификатором {} не найден.", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден!");
        }
    }

    @Override
    public User deleteUser(Long id) {
        return null;
    }

    @Override
    public User updateUser(User user) throws IOException {
        return null;
    }

    @Override
    public Map<Long, User> getUsers() {
        return null;
    }

    @Override
    public void clean() {

    }
}
