package ru.yandex.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.model.Mpa;
import ru.yandex.filmorate.storage.MpaDbStorage;
import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequestMapping("/mpa")
public class MpaController {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @GetMapping
    public Collection<Mpa> findAll() {
        return mpaDbStorage.getMpas();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable Long id) {
        if (mpaDbStorage.getMpa(id).isPresent()) {
            return mpaDbStorage.getMpa(id).get();
        }
        log.info("MPA с идентификатором {} не найден.", id);
        throw new NotFoundException("MPA с id = " + id + " не найден!");
    }

}

