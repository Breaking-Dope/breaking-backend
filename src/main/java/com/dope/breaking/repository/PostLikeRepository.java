package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike,Long>, PostLikeRepositoryCustom {

    boolean existsPostLikesByUserAndPost(User user, Post post);

    void deleteByUserAndPost(User user, Post post);

    int countPostLikesByUser(User user);

    int countPostLikesByPost(Post post);

    List<PostLike> findAllByPost(Post post);

    Boolean existsByUserAndPostId(User user, Long postId);

}
