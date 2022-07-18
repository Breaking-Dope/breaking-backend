package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

    boolean existsPostLikesByUserAndPost(User user, Post post);

}
