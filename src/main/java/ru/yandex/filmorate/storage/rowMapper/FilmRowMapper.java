package ru.yandex.filmorate.storage.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.filmorate.model.Film;
import java.sql.ResultSet;
import java.sql.SQLException;


public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film(rs.getString("NAME")
                , rs.getString("DESCRIPTION")
                , rs.getDate("RELEASE_DATE").toLocalDate()
                , rs.getInt("DURATION"));
        film.setId(rs.getLong("ID"));
        return film;
    }
}
