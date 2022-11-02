package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.model.Mpa;
import ru.yandex.filmorate.storage.rowMapper.MpaRowMapper;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Collection<Mpa> getMpas() {
        log.info("Поиск всех MPA");
        final String query = "SELECT * FROM mpa";
        Collection<Mpa> mpas = jdbcTemplate.query(
                query, new MpaRowMapper());
        log.info("Текущее количество MPA: {}", mpas.size());
        return mpas;
    }

    public Optional<Mpa> getMpa(Long id) {
        log.info("Поиск MPA по id " + id);
        final String query = "SELECT * FROM mpa WHERE id = ?";
        Mpa mpa = jdbcTemplate.queryForObject(
                query, new Object[] { id }, new MpaRowMapper());
        return mpa != null ? Optional.of(mpa) : Optional.empty();
    }
}
