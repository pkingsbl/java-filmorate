package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.User;
import ru.yandex.filmorate.storage.rowMapper.UserRowMapper;
import java.io.IOException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

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
        log.info("Поиск пользователя по id " + id);
        final String query = "select * from users where id = ?";
        User user = jdbcTemplate.queryForObject(
                query, new Object[] { id }, new UserRowMapper());
        return user != null ? Optional.of(user) : Optional.empty();
    }

    @Override
    public Map<Long, User> getUsers() {
        log.info("Поиск всех пользоватей");
        final String query = "SELECT * FROM users";
        Collection<User> users = jdbcTemplate.query(query, new UserRowMapper());
        Map<Long, User>  userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        log.info("Текущее количество пользоватей: {}", userMap.size());
        return userMap;
    }

    @Override
    public List<User> findFriends(Long id) {
        log.info("Поиск друзей пользователя id: {}", id);
        final String query = "SELECT * FROM users WHERE id IN (SELECT friend_id FROM friends WHERE user_id = ?)";
        List<User> friends = jdbcTemplate.query(query, new Object[] { id }, new UserRowMapper());
        log.info("У пользователя id: {}, количество исходящих заявок в друзья: {}", id, friends.size());
        return friends;
    }

    @Override
    public List<User> getMutualFriends(Long id, Long otherId) {
        final String query = "SELECT * FROM users WHERE id IN (SELECT friend_id " +
                "FROM friends WHERE user_id = " + id + ") " +
                "AND id IN (SELECT friend_id FROM friends where user_id = " + otherId + ")";

        List<User> friends = jdbcTemplate.query(query, new UserRowMapper());
        log.info("У пользователей id: {} и id: {}, количество общих друзей: {}", id, otherId, friends.size());
        return friends;
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
    public void deleteFriend(Long id, Long friendId) {
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? AND friend_id = ?", id, friendId);
    }
}
