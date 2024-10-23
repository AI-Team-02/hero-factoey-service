package ai.herofactoryservice.create_game_resource_service.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String taskId) {
        super("Task not found with id: " + taskId);
    }
}