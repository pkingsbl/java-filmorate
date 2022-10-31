package ru.yandex.filmorate.storage;

import ru.yandex.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Long id);

    Collection<Film> getFilms();

    Optional<Film> getFilm(Long id);

    void addLikeFilm(Long film_id, Long user_id);

    void deleteLikeFilm(Long userId, Long filmId);

    List<Film> getPopular(int count);
}
