package com.dope.breaking.repository;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.comment.CommentLike;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike,Long> {

    boolean existsCommentLikeByUserAndComment(User user, Comment comment);

    boolean existsCommentLikeByUserAndCommentId(User user, Long comment_id);

    void deleteByUserAndComment(User user, Comment comment);

    int countCommentLikesByComment(Comment comment);

    List<CommentLike> findAllByComment(Comment comment);

}