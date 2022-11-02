package ru.yandex.filmorate.storage.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.filmorate.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(rs.getLong("ID")
                , rs.getString("EMAIL")
                , rs.getString("LOGIN")
                , rs.getString("NAME")
                , rs.getDate("BIRTHDAY").toLocalDate());
    }
}
