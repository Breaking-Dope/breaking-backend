package com.dope.breaking.repository;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    List<Comment> findAllByPost(Post post);

    int countByPost(Post post);

}
