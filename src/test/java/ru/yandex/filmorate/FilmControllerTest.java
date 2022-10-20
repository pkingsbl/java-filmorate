package ru.yandex.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.filmorate.controller.FilmController;
import ru.yandex.filmorate.model.Film;
import ru.yandex.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private FilmController filmController;
    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    public void afterEach() {
        filmStorage.clean();
    }

    @Test
    void postValidFilmTest() throws Exception {
        Film film = new Film("Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        String body = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void putValidUpdateNameFilmTest() throws Exception {
        Film film = new Film("Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        String body = objectMapper.writeValueAsString(film);
        System.out.println(body);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
        Film filmUpdate = new Film("Ку! Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        filmUpdate.setId(1L);
        body = objectMapper.writeValueAsString(filmUpdate);
        System.out.println(body);

        this.mockMvc.perform(put("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void putNotFoundFilmTest() throws Exception {
        Film film = new Film("Ку! Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        film.setId(2L);
        String body = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(put("/films")
                        .content(body)
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getValidFilmTest() throws Exception {
        Film film = new Film("Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        String body = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        Film filmSec = new Film("Ку! Кин-дза-дза!"
                , "полнометражный фантастический анимационный фильм"
                , LocalDate.of(2013, 4, 11), 6);
        body = objectMapper.writeValueAsString(filmSec);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Кин-дза-дза!"))
                .andExpect(jsonPath("$[1].name").value("Ку! Кин-дза-дза!"));
    }

    @Test
    void postFilmWithoutNameTest() throws Exception {
        Film film = new Film(" "
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        String body = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postFilmInvalidDescriptionTest() throws Exception {
        Film film = new Film("Кин-дза-дза!"
                , RandomString.make(201)
                , LocalDate.of(1986, 12, 1), 8);
        String body = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNegativeFilmTest() throws Exception {
        this.mockMvc.perform(get("/films/-1"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void putDeleteNegativeFilmTest() throws Exception {
        this.mockMvc.perform(delete("/films/-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}