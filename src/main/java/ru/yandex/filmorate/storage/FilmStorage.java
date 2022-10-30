package ru.yandex.filmorate.storage;

import ru.yandex.filmorate.model.Film;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Long id);

    Map<Long, Film> getFilms();

    void clean();

    Optional<Film> getFilm(Long id);

    public void addLikeFilm(Long film_id, Long user_id);

    public void deleteLikeFilm(Long userId, Long filmId);

    public List<Film> getPopular(int count);
}
