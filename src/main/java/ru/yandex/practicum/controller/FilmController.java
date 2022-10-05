package ru.yandex.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Film;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private int idFilm = 1;

    private final Map<Integer, Film> films = new HashMap<>();

    public void clean(){
        this.films.clear();
        this.idFilm = 1;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        log.debug("POST /films добавление фильма");
        checkReleaseDate(film.getReleaseDate());
        film.setId(idFilm++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: id = {}, name = {}", film.getId(), film.getName());
        return film;
    }

    private static void checkReleaseDate(LocalDate releaseDate) throws ValidationException {
        if(releaseDate.isBefore(LocalDate.of(1895,12,28))) {
            throw new ValidationException("Дата релиза ранее 28 декабря 1895 года");
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) throws NotFoundException {
        log.debug("PUT /films обновление фильма");
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден!");
        }
        films.put(film.getId(), film);
        log.info("Обновлен фильм: id = {}, name = {}", film.getId(), film.getName());
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("GET /films получение всех фильмов");
        log.info("Текущее количество фильмов: {}", films.size());
        return films.values();
    }

}
