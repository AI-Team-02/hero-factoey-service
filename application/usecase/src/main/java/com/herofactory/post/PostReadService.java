package com.herofactory.post;

import com.herofactory.PostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostReadService implements PostReadUsecase {
    private final PostPort postPort;

    @Override
    public ResolvedPost getById(Long id) {
        return postPort.findResolvedPostById(id);
    }
}
