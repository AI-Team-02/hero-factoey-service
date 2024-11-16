package com.herofactory.post;


public class PostEntityConverter {

    public static PostEntity toEntity(Post post, CategoryEntity category) {
        return new PostEntity(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUserId(),
                category,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getDeletedAt()
        );
    }

    public static Post toPostModel(PostEntity postEntity) {
        return new Post(
                postEntity.getId(),
                postEntity.getTitle(),
                postEntity.getContent(),
                postEntity.getUserId(),
                postEntity.getCategory().getId(),
                postEntity.getCreatedAt(),
                postEntity.getUpdatedAt(),
                postEntity.getDeletedAt()
        );
    }

    public static ResolvedPost toResolvedPostModel(PostEntity postEntity) {
        return new ResolvedPost(
                postEntity.getId(),
                postEntity.getTitle(),
                postEntity.getContent(),
                postEntity.getUserId(),
                postEntity.getCategory().getId(),
                postEntity.getCategory().getName(),
                postEntity.getCreatedAt(),
                postEntity.getUpdatedAt());
    }
}
