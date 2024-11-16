package com.herofactory.post;

import lombok.Data;

public interface PostDeleteUsecase {

    Post delete(Request request);

    @Data
    class Request {
        private final Long postId;
    }
}
