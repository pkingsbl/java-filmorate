package ru.yandex.practicum.model;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Slf4j
@Data
public class Film {

    private int id;
    @NotBlank
    private final String name;
    @Size(max = 200)
    private final String description;
    @NotNull
    private final LocalDate releaseDate;
    @Positive
    private final int duration;

}
