package com.herofactory.controller;


import com.herofactory.model.PostCreateRequest;
import com.herofactory.model.PostDetailDto;
import com.herofactory.model.PostDto;
import com.herofactory.model.PostUpdateRequest;
import com.herofactory.post.Post;
import com.herofactory.post.PostCreateUsecase;
import com.herofactory.post.PostDeleteUsecase;
import com.herofactory.post.PostReadUsecase;
import com.herofactory.post.PostUpdateUsecase;
import com.herofactory.post.ResolvedPost;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/post")
public class PostController {

    private final PostCreateUsecase postCreateUsecase;
    private final PostUpdateUsecase postUpdateUsecase;
    private final PostDeleteUsecase postDeleteUsecase;
    private final PostReadUsecase postReadUsecase;

    @PostMapping
    ResponseEntity<PostDto> createPost(
            @RequestBody PostCreateRequest request
    ) {
        Post post = postCreateUsecase.create(
                new PostCreateUsecase.Request(
                        request.getUserId(),
                        request.getTitle(),
                        request.getContent(),
                        request.getCategoryId()
                )
        );
        return ResponseEntity.ok().body(toDto(post));
    }

    @PutMapping("/{postId}")
    ResponseEntity<PostDto> updatePost(
            @PathVariable("postId") Long id,
            @RequestBody PostUpdateRequest request
    ) {
        Post post = postUpdateUsecase.update(
                new PostUpdateUsecase.Request(
                        id,
                        request.getTitle(),
                        request.getContent(),
                        request.getCategoryId()
                )
        );
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(toDto(post));
    }

    @DeleteMapping("/{postId}")
    ResponseEntity<PostDto> deletePost(
            @PathVariable("postId") Long id
    ) {
        Post post = postDeleteUsecase.delete(new PostDeleteUsecase.Request(id));
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(toDto(post));
    }

    @GetMapping("/{postId}/detail")
    ResponseEntity<PostDetailDto> readPostDetail(
            @PathVariable("postId") Long id
    ) {
        ResolvedPost resolvedPost = postReadUsecase.getById(id);
        if (resolvedPost == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(toDto(resolvedPost));
    }

    private PostDto toDto(Post post) {
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUserId(),
                post.getCategoryId(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getDeletedAt()
        );
    }

    private PostDetailDto toDto(ResolvedPost resolvedPost) {
        return new PostDetailDto(
                resolvedPost.getId(),
                resolvedPost.getTitle(),
                resolvedPost.getContent(),
                resolvedPost.getUserId(),
                resolvedPost.getCategoryName(),
                resolvedPost.getCreatedAt(),
                resolvedPost.getUpdatedAt()
        );
    }
}
