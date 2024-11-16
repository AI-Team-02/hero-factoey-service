package com.herofactory.post;

import com.herofactory.PostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostDeleteService implements PostDeleteUsecase {

    private final PostPort postPort;
//    private final OriginalPostMessageProducePort originalPostMessageProducePort;


    // 가장 간단하지만, kafka 로직 뒤에 에러발생시 가짜 메시지 발행됨 -> 프로젝트에 맞춰서 적용권장 CDC
    @Transactional
    @Override
    public Post delete(PostDeleteUsecase.Request request) {
        Post post = postPort.findById(request.getPostId());
        if (post == null) return null;
        post.delete();

        Post deletedPost= postPort.save(post); // soft delete
//        originalPostMessageProducePort.sendDeleteMessage(deletedPost.getId());
        return deletedPost;
    }
}
