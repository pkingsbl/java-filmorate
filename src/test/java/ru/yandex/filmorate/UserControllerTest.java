package ru.yandex.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.filmorate.controller.UserController;
import ru.yandex.filmorate.model.User;
import ru.yandex.filmorate.storage.UserStorage;

import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private UserController userController;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    public void afterEach() {
        userStorage.clean();
    }

    @Test
    void postValidUserTest() throws Exception {
        User user = new User(null,"niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void putValidUpdateUserTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        User userUpdate = new User(1L, "niktoneponyal@gmail.com"
                , "anigilyator2000", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        body = objectMapper.writeValueAsString(userUpdate);
        this.mockMvc.perform(put("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void getValidUserTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        User userSec = new User(null, "niktoneponyal@gmail.com"
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
        User user = new User(null, "niktoneponyal@gmail.com"
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
        User user = new User(null, "niktoniktoneponyal@gmail.comneponyal"
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
        User user = new User(null, "niktoneponyal"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postValidUserWithoutNameTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", ""
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("pkingsbl"));
    }

}