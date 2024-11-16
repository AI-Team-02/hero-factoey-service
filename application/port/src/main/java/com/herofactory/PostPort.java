package com.herofactory;

import com.herofactory.post.Post;
import com.herofactory.post.ResolvedPost;

public interface PostPort {
    Post save(Post post);

    Post findById(Long id);

    ResolvedPost findResolvedPostById(Long id);

}
