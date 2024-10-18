package com.gameservice.create_game_resource_service.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GameResourceCreationRequest {
    private String name;
    private String description;
    private String prompt;

    public GameResourceCreationRequest(){}

    @JsonCreator
    public GameResourceCreationRequest(@JsonProperty("name") String name,@JsonProperty("description") String description,@JsonProperty("prompt") String prompt) {
        this.name = name;
        this.description = description;
        this.prompt = prompt;
    }

}