package com.herofactory.post;

import com.herofactory.PostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostUpdateService implements PostUpdateUsecase {

    private final PostPort postPort;
//    private final OriginalPostMessageProducePort originalPostMessageProducePort;

    @Transactional
    @Override
    public Post update(PostUpdateUsecase.Request request) {
        Post post = postPort.findById(request.getPostId());
        if (post == null) return null;
        post.update(
            request.getTitle(),
            request.getContent(),
            request.getCategoryId()
        );

        Post savedPost = postPort.save(post);
//        originalPostMessageProducePort.sendUpdateMessage(savedPost);
        return savedPost;
    }
}
