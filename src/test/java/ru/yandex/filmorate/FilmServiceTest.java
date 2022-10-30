package ru.yandex.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.filmorate.controller.FilmController;
import ru.yandex.filmorate.model.Film;
import ru.yandex.filmorate.model.Mpa;
import ru.yandex.filmorate.model.User;
import ru.yandex.filmorate.storage.FilmDbStorage;
import java.time.LocalDate;
import java.util.Random;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmServiceTest {

    @Autowired
    private FilmController filmController;
    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;


    @Test
    void putAddLikeFilmTest() throws Exception {
        Film film = new Film("Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        film.setMpa(new Mpa(1, null));
        String body = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
        User user = new User(null,"niktoneponyal@gmail.com"
                , "kingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void putDeleteLikeFilmTest() throws Exception {
        Film film = new Film("Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        film.setMpa(new Mpa(1, null));
        String body = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
        User user = new User(null,"niktoneponyal@gmail.com"
                , "sam", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk());
        this.mockMvc.perform(delete("/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getPopularFilmsTest() throws Exception {
        for (int i = 1; i < 21; i++) {
            User user = new User(null,"bot@gmail.com"
                    , ("ya_tochno_ne_bot-" + i), "Fialeta"
                    , LocalDate.of((1922 + i), 12, 1));
            String body = objectMapper.writeValueAsString(user);
            this.mockMvc.perform(post("/users")
                            .content(body)
                            .contentType("application/json"))
                    .andExpect(status().isOk());
        }

        for (int i = 1; i < 10; i++) {
            Film film = new Film(("Фиьмс-" + i)
                    , "Это совершенно обыкновенный фильм с купленным рейтингом"
                    , LocalDate.of((2000 + i), 12, 1), 10);
            film.setMpa(new Mpa(1, null));
            String body = objectMapper.writeValueAsString(film);
            this.mockMvc.perform(post("/films")
                            .content(body)
                            .contentType("application/json"))
                    .andExpect(status().isOk());

            for (int j = 1; j < (new Random().nextInt(19) + 2); j++) {
                this.mockMvc.perform(put(String.format("/films/%d/like/%d", i, j)))
                        .andDo(print())
                        .andExpect(status().isOk());
            }
        }
        this.mockMvc.perform(get("/films/popular")
                        .param("count", "5"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void putDeleteLikeNegativeFilmTest() throws Exception {
        this.mockMvc.perform(delete("/films/-1/like/-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNegativePopularFilmTest() throws Exception {
        this.mockMvc.perform(get("/films/popular")
                        .param("count", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putNegativeLikeNegativeFilmTest() throws Exception {
        this.mockMvc.perform(put("/films/-1/like/-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}