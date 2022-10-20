package ru.yandex.filmorate.storage;

import ru.yandex.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film deleteFilm(Long id);

    Map<Long, Film> getFilms();

    void clean();

    Film getFilm(Long id);
}
