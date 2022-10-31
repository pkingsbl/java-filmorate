package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.User;
import java.io.IOException;
import java.sql.Date;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()){
            user.setName(user.getLogin());
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин содержит пробелы");
        }
        jdbcTemplate.update(
                "INSERT INTO users (email, login, name , birthday) VALUES (?, ?, ?, ?)",
                user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()));
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT id FROM users WHERE login = ?", user.getLogin());

        if(userRows.next()){
            user.setId(userRows.getLong("id"));
        }
        log.info("Добавлен пользователь: id = {}, login = {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public Optional<User> getUser(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where id = ?", id);

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
        }
        return Optional.empty();
    }

    @Override
    public void deleteUser(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        if (!userRows.next()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден!");
        }
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ?", id);
        jdbcTemplate.update("DELETE FROM friends WHERE friend_id = ?", id);
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public User updateUser(User user) throws IOException {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", user.getId());
        if (!userRows.next()) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден!");
        }

        jdbcTemplate.update("UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?"
                , user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());
        return user;
    }

    @Override
    public Map<Long, User> getUsers() {
        Map<Long, User>  users = new HashMap<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users");
        while (userRows.next()) {
            User user = new User(userRows.getLong("id")
                    , userRows.getString("email")
                    , userRows.getString("login")
                    , userRows.getString("name")
                    , Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate());
            users.put(userRows.getLong("id"), user);
        }
        return users;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ? OR id = ?", id, friendId);
        if ((!userRows.next()
                || (userRows.getLong("id") != id && userRows.getLong("id") != friendId))
                || (!userRows.next()
                || (userRows.getLong("id") != id && userRows.getLong("id") != friendId))) {
            throw new NotFoundException("Пользователь не найден!");
        }
        jdbcTemplate.update("INSERT INTO friends (user_id, friend_id) VALUES (?, ?)", id, friendId);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? AND friend_id = ?", id, friendId);
    }

    @Override
    public List<User> findFriends(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?", id);
        if (!userRows.next()) {
            throw new NotFoundException("Пользователь не найден!");
        }
        final String sql = "SELECT * FROM users WHERE id IN (SELECT friend_id FROM friends WHERE user_id = ?)";
        List<User> friends = new ArrayList<>();
        userRows = jdbcTemplate.queryForRowSet(sql, id);
        while (userRows.next()) {
            User user = new User(userRows.getLong("id")
                    , userRows.getString("email")
                    , userRows.getString("login")
                    , userRows.getString("name")
                    , Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate());
            friends.add(user);
        }
        return friends;
    }

    @Override
    public List<User> getMutualFriends(Long id, Long otherId) {
        final String sql = "SELECT * FROM users WHERE id IN (SELECT friend_id " +
                "FROM friends WHERE user_id = " + id + ") " +
                "AND id IN (SELECT friend_id FROM friends where user_id = " + otherId + ")";

        List<User> friends = new ArrayList<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sql);
        while (userRows.next()) {
            User user = new User(userRows.getLong("id")
                    , userRows.getString("email")
                    , userRows.getString("login")
                    , userRows.getString("name")
                    , Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate());
            friends.add(user);
        }
        return friends;
    }
}
