package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

    boolean existsPostLikesByUserAndPost(User user, Post post);

    Optional<PostLike> findPostLikeByUserAndPost(User user, Post post);

}
