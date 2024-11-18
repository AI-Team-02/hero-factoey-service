package com.herofactory.prompt.service;

import com.herofactory.prompt.entity.Prompt;
import com.herofactory.prompt.repository.CustomVectorRepository;
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
}