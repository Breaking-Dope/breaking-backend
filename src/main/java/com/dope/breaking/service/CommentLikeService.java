package com.dope.breaking.service;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.comment.CommentLike;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.comment.NoSuchCommentException;
import com.dope.breaking.exception.like.AlreadyLikedException;
import com.dope.breaking.exception.like.AlreadyUnlikedException;
import com.dope.breaking.repository.CommentLikeRepository;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void likeComment (String username, Long commentId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Comment comment = commentRepository.findById(commentId).orElseThrow(NoSuchCommentException::new);

        if (commentLikeRepository.existsCommentLikeByUserAndComment(user,comment)){
            throw new AlreadyLikedException();
        }

        CommentLike commentLike = new CommentLike(user,comment);
        commentLikeRepository.save(commentLike);

    }

    @Transactional
    public void unlikeComment (String username, Long commentId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Comment comment = commentRepository.findById(commentId).orElseThrow(NoSuchCommentException::new);

        if (!commentLikeRepository.existsCommentLikeByUserAndComment(user,comment)){
            throw new AlreadyUnlikedException();
        }

        commentLikeRepository.deleteByUserAndComment(user,comment);

    }

}
