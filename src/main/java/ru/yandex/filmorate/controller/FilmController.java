package ru.yandex.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.Film;
import ru.yandex.filmorate.service.FilmService;
import ru.yandex.filmorate.storage.FilmStorage;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/films")
public class FilmController {

    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private FilmService filmService;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        return filmStorage.addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) throws NotFoundException {
        return filmStorage.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable @Min(value = 1, message = "film id должен быть больше 0") Long id
            , @PathVariable @Min(value = 1, message = "user id должен быть больше 0") Long userId) {
        return filmService.addLike(userId, id);
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Текущее количество фильмов: {}", filmStorage.getFilms().size());
        return new ArrayList<>(filmStorage.getFilms().values());
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Min(value = 1, message = "film id должен быть больше 0") Long id) {
        return filmStorage.getFilm(id);
    }

    @GetMapping("/popular")
    public List<Film> showMostPopularFilms(@RequestParam(defaultValue = "10") @Min(value = 1
            , message = "параметр count должен быть больше 0") Integer count) {
        return new ArrayList<>(filmService.getPopularFilms(count));
    }

    @DeleteMapping("/{id}")
    public Film deleteFilm(@PathVariable @Min(value = 1, message = "film id должен быть больше 0") Long id) {
        return filmStorage.deleteFilm(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable @Min(value = 1, message = "film id должен быть больше 0") Long id,
            @PathVariable Long userId) {
        return filmService.deleteLike(userId, id);
    }

}

