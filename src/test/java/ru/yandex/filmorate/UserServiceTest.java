package ru.yandex.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserServiceTest {

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
    void putAddFriendUserTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
        User userFriend = new User(null, "niktoneponyal@gmail.com"
                , "anigilyator2000", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        body = objectMapper.writeValueAsString(userFriend);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/users/1"))
                .andDo(print())
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/users/2"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void deleteFriendUserTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
        User userFriend = new User(null, "niktoneponyal@gmail.com"
                , "anigilyator2000", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        body = objectMapper.writeValueAsString(userFriend);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());
        this.mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUserTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserByIdTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserFriendsTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        for (int i = 2; i < 6; i++) {
            User userFriend = new User(null,"bot_hihi@gmail.com"
                    , ("real_frend-" + i), "GG"
                    , LocalDate.of((1922 + i), 12, 1));
            body = objectMapper.writeValueAsString(userFriend);
            this.mockMvc.perform(post("/users")
                            .content(body)
                            .contentType("application/json"))
                    .andExpect(status().isOk());
            this.mockMvc.perform(put(String.format("/users/1/friends/%d", i)))
                    .andExpect(status().isOk());
        }

        this.mockMvc.perform(get("/users/1/friends"))
                .andExpect(jsonPath("$[0].friends").value(1))
                .andExpect(jsonPath("$[1].friends").value(1))
                .andExpect(jsonPath("$[2].friends").value(1))
                .andExpect(jsonPath("$[3].friends").value(1))
                .andExpect(status().isOk());
        this.mockMvc.perform(get("/users/1"))
                .andDo(print())
                .andExpect(jsonPath("$.friends").value(Lists.newArrayList(2, 3, 4, 5)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserFindCommonFriendsTest() throws Exception {
        for (int i = 1; i < 6; i++) {
            User userFriend = new User(null,"bot_hihi@gmail.com"
                    , ("real_frend-" + i), "GG"
                    , LocalDate.of((1922 + i), 12, 1));
            String body = objectMapper.writeValueAsString(userFriend);
            this.mockMvc.perform(post("/users")
                            .content(body)
                            .contentType("application/json"))
                    .andExpect(status().isOk());
        }
        for (int i = 2; i < 6; i++) {
            this.mockMvc.perform(put(String.format("/users/1/friends/%d", i)))
                    .andExpect(status().isOk());
        }
        for (int i = 4; i < 6; i++) {
            this.mockMvc.perform(put(String.format("/users/3/friends/%d", i)))
                    .andExpect(status().isOk());
        }

        List<Integer> commonFriends = Lists.newArrayList(1,3);
        this.mockMvc.perform(get("/users/1/friends/common/3"))
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[1].id").value(5))
                .andExpect(jsonPath("$[0].friends").value(commonFriends))
                .andExpect(jsonPath("$[1].friends").value(commonFriends))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void putAddNegativeFriendUserTest() throws Exception {
        this.mockMvc.perform(put("/users/-1/friends/-2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteNegativeFriendUserTest() throws Exception {
        this.mockMvc.perform(delete("/users/-1/friends/-2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteNegativeUserTest() throws Exception {
        this.mockMvc.perform(delete("/users/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserByNegativeIdTest() throws Exception {
        this.mockMvc.perform(get("/users/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserByUnknownIdTest() throws Exception {
        this.mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNegativeIdUserFriendsTest() throws Exception {
        this.mockMvc.perform(get("/users/-1/friends"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNegativeUserFindCommonFriendsTest() throws Exception {
        this.mockMvc.perform(get("/users/-1/friends/common/-3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putGetFriendsTest() throws Exception {
        User user = new User(null, "niktoneponyal@gmail.com"
                , "pkingsbl", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        String body = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
        User userFriend = new User(null, "niktoneponyal@gmail.com"
                , "anigilyator2000", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        body = objectMapper.writeValueAsString(userFriend);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());
        User userSecondFriend = new User(null, "niktoneponyal@gmail.com"
                , "anigilyator2000", "Vialeta"
                , LocalDate.of(1922, 12, 1));
        body = objectMapper.writeValueAsString(userSecondFriend);
        this.mockMvc.perform(post("/users")
                        .content(body)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users/1/friends/common/2"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/users/1/friends/-1"))
                .andExpect(status().isNotFound());

        this.mockMvc.perform(get("/users/1/friends"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users/2/friends"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users/1/friends/common/2"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/users/1/friends/3"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users/1/friends"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/users/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

}