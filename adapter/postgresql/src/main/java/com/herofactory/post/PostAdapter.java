package com.herofactory.post;


import com.herofactory.PostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PostAdapter implements PostPort {

    private final PostJpaRepository postJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;


    @Override
    public Post save(Post post) {
        CategoryEntity category = categoryJpaRepository.findById(post.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        PostEntity postEntity = postJpaRepository.save(PostEntityConverter.toEntity(post, category));
        return PostEntityConverter.toPostModel(postEntity);
    }

    @Override
    public Post findById(Long id) {
        PostEntity postEntity = postJpaRepository.findById(id).orElse(null);
        if (postEntity == null) return null;
        return PostEntityConverter.toPostModel(postEntity);
    }

    @Override
    public ResolvedPost findResolvedPostById(Long id) {
        PostEntity postEntity = postJpaRepository.findById(id).orElse(null);
        if (postEntity == null) return null;
        return PostEntityConverter.toResolvedPostModel(postEntity);
    }
}
