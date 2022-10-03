package ru.yandex.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.controller.FilmController;
import ru.yandex.practicum.controller.UserController;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.User;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTest {

    @Autowired
    private FilmController filmController;
    @Autowired
    private UserController userController;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    public void afterEach() {
        filmController.clean();
        userController.clean();
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
    void putValidFilmTest() throws Exception {
        Film film = new Film("Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        String body = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
        Film filmUpdate = new Film("Ку! Кин-дза-дза!"
                , "советская двухсерийная трагикомедия в жанре фантастической антиутопии"
                , LocalDate.of(1986, 12, 1), 8);
        filmUpdate.setId(1);
        body = objectMapper.writeValueAsString(filmUpdate);
        this.mockMvc.perform(put("/films")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
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
    void postValidUserTest() throws Exception {
        User user = new User("niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void putValidUserTest() throws Exception {
        User user = new User("niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        User userUpdate = new User("niktoneponyal@gmail.com"
                , "anigilyator2000", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        userUpdate.setId(1);
        body = objectMapper.writeValueAsString(userUpdate);
        this.mockMvc.perform(put("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void getValidUserTest() throws Exception {
        User user = new User("niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        User userSec = new User("niktoneponyal@gmail.com"
                , "anigilyator2000", "Vi"
                , LocalDate.of(2000, 12, 1));
        body = objectMapper.writeValueAsString(userSec);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("pkingsbl"))
                .andExpect(jsonPath("$[1].login").value("anigilyator2000"));
    }

    @Test
    void postUserWithoutLoginTest() throws Exception {
        User user = new User("niktoneponyal@gmail.com"
                , " ", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUserInvalidDateTest() throws Exception {
        User user = new User("niktoniktoneponyal@gmail.comneponyal"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(2222, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUserInvalidEmailTest() throws Exception {
        User user = new User("niktoneponyal"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

//    @Test
//    void postValidUserWithoutNameTest() throws Exception {
//        User user = new User("niktoneponyal@gmail.com"
//                , "pkingsbl", " "
//                , LocalDate.of(1922, 12, 1));
//        String body = objectMapper.writeValueAsString(user);
//        this.mockMvc.perform(post("/users")
//                        .content(body)
//                        .contentType("application/json"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("pkingsbl"));
//    }

}