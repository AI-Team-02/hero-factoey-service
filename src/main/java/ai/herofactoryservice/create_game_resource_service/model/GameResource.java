package ai.herofactoryservice.create_game_resource_service.model;

import lombok.Data;

@Data
public class GameResource {
    private String id;
    private String name;
    private String type;
    private String description;
    private String imageUrl;
}