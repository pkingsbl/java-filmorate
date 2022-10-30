package ru.yandex.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Mpa {

    @JsonProperty("id")
    final long id;
    @JsonProperty("name")
    String name;

}
