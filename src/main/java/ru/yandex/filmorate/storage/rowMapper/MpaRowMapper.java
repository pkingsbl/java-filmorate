package ru.yandex.filmorate.storage.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.filmorate.model.Mpa;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MpaRowMapper implements RowMapper<Mpa> {
    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getLong("ID")
                , rs.getString("NAME"));
    }
}
