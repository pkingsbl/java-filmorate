package ru.yandex.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage{

    private Long idFilm = 1L;

    private final Map<Long, Film> films = new HashMap<>();

    public Map<Long, Film> getFilms() {
        return films;
    }

    @Override
    public void clean() {
        films.clear();
        idFilm = 1L;
    }

    @Override
    public Film getFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
        return films.get(id);
    }

    @Override
    public Film addFilm(Film film) {
        log.debug("Добавление фильма");
        checkReleaseDate(film.getReleaseDate());
        film.setId(idFilm++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: id = {}, name = {}", film.getId(), film.getName());
        return film;

    }

    private static void checkReleaseDate(LocalDate releaseDate) {
        if(releaseDate.isBefore(LocalDate.of(1895,12,28))) {
            throw new ValidationException("Дата релиза ранее 28 декабря 1895 года");
        }
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("Обновление фильма");
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден!");
        }
        films.put(film.getId(), film);
        log.info("Обновлен фильм: id = {}, name = {}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film deleteFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
        return films.remove(id);
    }

}
