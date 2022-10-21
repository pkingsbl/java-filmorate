package ru.yandex.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private UserStorage userStorage;

    public Film addLike(Long userId, Long filmId) {
        if (filmStorage.getFilms().containsKey(filmId) && userStorage.getUsers().containsKey(userId)) {
            filmStorage.getFilms().get(filmId).getLikes().add(userId);
            log.info("Пользователь " + userStorage.getUsers().get(userId).getLogin()
                    + " поставил лайк " + filmStorage.getFilms().get(filmId).getName());
            return filmStorage.getFilms().get(filmId);
        } else {
            throw new NotFoundException("Пользователь или фильм не найдены");
        }
    }

    public Film deleteLike(Long userId, Long filmId) {
        if (filmStorage.getFilms().containsKey(filmId) && userStorage.getUsers().containsKey(userId)) {
            filmStorage.getFilms().get(filmId).getLikes().remove(userId);
            log.info("Пользователь " + userStorage.getUsers().get(userId).getLogin()
                    + " убрал лайк " + filmStorage.getFilms().get(filmId).getName());
            return filmStorage.getFilms().get(filmId);
        } else {
            throw new NotFoundException("Пользователь или фильм не найдены");
        }
    }

    public List<Film> getPopularFilms(@Positive Integer count) {
        Comparator<Film> comparator = Comparator.comparing(obj -> -obj.getLikes().size());
        List<Film> sortedFilms = new ArrayList<>(filmStorage.getFilms().values());
        sortedFilms.sort(comparator);

        List<Film> topFilms = sortedFilms.stream().limit(count).collect(Collectors.toList());
        log.info("Список фильмов ТОП-" + (Math.min(topFilms.size(), count)) + " сформирован");
        return topFilms;
    }

}
