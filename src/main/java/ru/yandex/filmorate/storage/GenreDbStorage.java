package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.model.Genre;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Collection<Genre> getGenres() {
        Collection<Genre> genres = new ArrayList<>();
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM genres");
        while (genreRows.next()) {
            Genre mpa = new Genre(genreRows.getInt("id"), genreRows.getString("name"));
            genres.add(mpa);
        }
        return genres;
    }

    public Optional<Genre> getGenre(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT name FROM genres WHERE id = ?", id);
        if (genreRows.next()) {
            return Optional.of(new Genre(id, genreRows.getString("name")));
        } else {
            throw new NotFoundException("Жанр с id = " + id + " не найден!");
        }
    }

}
