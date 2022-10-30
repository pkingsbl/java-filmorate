package ru.yandex.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.model.Film;
import ru.yandex.filmorate.storage.FilmStorage;
import ru.yandex.filmorate.storage.UserStorage;
import javax.validation.constraints.Positive;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    @Qualifier("FilmDbStorage")
    @Autowired
    private FilmStorage filmStorage;
    @Qualifier("UserDbStorage")
    @Autowired
    private UserStorage userStorage;

    public Film addLike(Long userId, Long filmId) {
        if (filmStorage.getFilms().containsKey(filmId) && userStorage.getUsers().containsKey(userId)) {
            filmStorage.addLikeFilm(filmId, userId);
            log.info("Пользователь id = " + userId
                    + " поставил лайк " + filmStorage.getFilms().get(filmId).getName());
            return filmStorage.getFilms().get(filmId);
        } else {
            throw new NotFoundException("Пользователь или фильм не найдены");
        }
    }

    public Film deleteLike(Long userId, Long filmId) {
        if (filmStorage.getFilms().containsKey(filmId) && userStorage.getUsers().containsKey(userId)) {
            filmStorage.deleteLikeFilm(userId, filmId);
            log.info("Пользователь id = " + userId
                    + " убрал лайк " + filmStorage.getFilms().get(filmId).getName());
            return filmStorage.getFilms().get(filmId);
        } else {
            throw new NotFoundException("Пользователь или фильм не найдены");
        }
    }

    public List<Film> getPopularFilms(@Positive Integer count) {
        List<Film> topFilms = filmStorage.getPopular(count);
        log.info("Список фильмов ТОП-" + (Math.min(topFilms.size(), count)) + " сформирован");
        return topFilms;
    }

}
