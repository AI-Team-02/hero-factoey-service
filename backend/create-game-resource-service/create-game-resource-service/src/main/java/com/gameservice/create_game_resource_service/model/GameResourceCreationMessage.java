package com.gameservice.create_game_resource_service.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Base64;

@NoArgsConstructor
@Getter
@Setter
public class GameResourceCreationMessage implements Serializable {
    @JsonProperty("taskId")
    private String taskId;
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("request")
    private GameResourceCreationRequest request;
    @JsonProperty("fileContent")
    private String fileContent;  // This will store the Base64 encoded string

    @JsonCreator
    private GameResourceCreationMessage(
            @JsonProperty("taskId")String taskId,
            @JsonProperty("fileName")String fileName,
            @JsonProperty("request")GameResourceCreationRequest request,
            @JsonProperty("fileContent")String fileContent) {
        this.taskId = taskId;
        this.fileName = fileName;
        this.request = request;
        this.fileContent = fileContent;
    }

    @JsonIgnore
    public byte[] getDecodedFileContent() {
        return Base64.getDecoder().decode(fileContent);
    }

    @JsonIgnore
    public void setDecodedFileContent(byte[] fileContent) {
        this.fileContent = Base64.getEncoder().encodeToString(fileContent);
    }
}