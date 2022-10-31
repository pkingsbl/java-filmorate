package ru.yandex.filmorate.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Data
@AllArgsConstructor
//@NoArgsConstructor
public class User {

    private Long id;
    @Email
    private String email;
    @NotBlank
    private String login;
    private String name;
    @Past
    private LocalDate birthday;
    @Getter
    private final Set<Long> friends = new HashSet<>();

}
