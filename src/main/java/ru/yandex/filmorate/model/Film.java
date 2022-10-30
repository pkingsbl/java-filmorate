package ru.yandex.filmorate.model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Slf4j
@Data
public class Film {

    private Long id;
    @NotBlank
    private final String name;
    @Size(max = 200)
    private final String description;
    @NotNull
    private final LocalDate releaseDate;
    @Positive
    private final int duration;
    private Set<Long> likes = new HashSet<>();
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @JsonProperty("mpa")
    private Mpa mpa;
    private List<Genre> genres;

}
