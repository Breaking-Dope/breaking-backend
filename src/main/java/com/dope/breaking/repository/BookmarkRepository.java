package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Bookmark;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserAndPost(User user, Post post);

    int countByPost(Post post);

    void deleteByUserAndPost(User user, Post post);

    Boolean existsByUserAndPostId(User me, Long postId);
}
