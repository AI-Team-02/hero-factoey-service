package com.herofactory.post;

import com.herofactory.PostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostCreateService implements PostCreateUsecase {

    private final PostPort postPort;
//    private final OriginalPostMessageProducePort originalPostMessageProducePort;

    @Transactional
    @Override
    public Post create(Request request) {
        Post post =  postPort.save(
            Post.generate(
                request.getUserId(),
                request.getTitle(),
                request.getContent(),
                request.getCategoryId()
            )
        );

//        originalPostMessageProducePort.sendCreateMessage(post);
        return post;
    }
}
