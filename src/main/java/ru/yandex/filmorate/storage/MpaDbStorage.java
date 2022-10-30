package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.model.Mpa;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Collection<Mpa> getMpas() {
        Collection<Mpa> mpas = new ArrayList<>();
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM mpa");
        while (mpaRows.next()) {
            Mpa mpa = new Mpa(mpaRows.getLong("id"), mpaRows.getString("name"));
            mpas.add(mpa);
        }
        return mpas;
    }

    public Optional<Mpa> getMpa(Long id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT name FROM mpa WHERE id = ?", id);
        if (mpaRows.next()) {
            return Optional.of(new Mpa(id, mpaRows.getString("name")));
        } else {
            throw new NotFoundException("Рейтинг с id = " + id + " не найден!");
        }
    }

}
