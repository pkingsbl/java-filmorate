package ru.yandex.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.filmorate.model.Genre;
import ru.yandex.filmorate.storage.GenreDbStorage;
import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequestMapping("/genres")
public class GenreController {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @GetMapping
    public Collection<Genre> findAll() {
        return genreDbStorage.getGenres();
    }

    @GetMapping("/{id}")
    public Genre getMpaById(@PathVariable int id) {
        return genreDbStorage.getGenre(id).get();
    }

}

