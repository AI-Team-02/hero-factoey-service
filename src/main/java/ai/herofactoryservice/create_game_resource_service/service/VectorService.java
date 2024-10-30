package ai.herofactoryservice.create_game_resource_service.service;

import ai.herofactoryservice.create_game_resource_service.model.Prompt;
import ai.herofactoryservice.create_game_resource_service.repository.CustomVectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VectorService {
    private final CustomVectorRepository vectorRepository;

    @Transactional
    public void savePrompt(Prompt prompt) {
        vectorRepository.savePromptWithVector(prompt);
    }

    @Transactional(readOnly = true)
    public List<Prompt> findSimilarPrompts(double[] vector, double threshold, int limit) {
        return vectorRepository.findSimilarPrompts(vector, threshold, limit);
    }

    @Transactional
    public void initializeVectorSupport() {
        vectorRepository.createVectorExtensionAndIndex();
    }
}