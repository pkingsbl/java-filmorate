package ru.yandex.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.Film;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage{

    @Qualifier("inMemoryUserStorage")
    @Autowired
    private UserStorage userStorage;
    private Long idFilm = 1L;

    private final Map<Long, Film> films = new HashMap<>();

    public Map<Long, Film> getFilms() {
        return films;
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLikeFilm(Long film_id, Long user_id) {
        if (films.containsKey(film_id) && userStorage.getUsers().containsKey(user_id)) {
            films.get(film_id).getLikes().add(user_id);
            log.info("Пользователь " + userStorage.getUsers().get(user_id).getLogin()
                    + " поставил лайк " + films.get(film_id).getName());
        } else {
            throw new NotFoundException("Пользователь или фильм не найдены");
        }
    }

    @Override
    public void deleteLikeFilm(Long userId, Long filmId) {
        if (films.containsKey(filmId) && userStorage.getUsers().containsKey(userId)) {
            films.get(filmId).getLikes().remove(userId);
            log.info("Пользователь " + userStorage.getUsers().get(userId).getLogin()
                    + " убрал лайк " + films.get(filmId).getName());
        } else {
            throw new NotFoundException("Пользователь или фильм не найдены");
        }
    }

    @Override
    public List<Film> getPopular(int count) {
        Comparator<Film> comparator = Comparator.comparing(obj -> -obj.getLikes().size());
        List<Film> sortedFilms = new ArrayList<>(films.values());
        sortedFilms.sort(comparator);

        return sortedFilms.stream().limit(count).collect(Collectors.toList());
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
    public void deleteFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
    }

}
