package ru.yandex.filmorate.storage.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.filmorate.model.Genre;
import java.sql.ResultSet;
import java.sql.SQLException;


public class GenreRowMapper implements RowMapper<Genre> {
    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("ID")
                , rs.getString("NAME"));
    }
}
