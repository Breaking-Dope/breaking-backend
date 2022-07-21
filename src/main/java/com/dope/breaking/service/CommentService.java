package com.dope.breaking.service;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.comment.NoSuchCommentException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public void addCommentToPost (Long postId, String username, String content){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        Comment comment = new Comment(user, post, content);

        commentRepository.save(comment);
        userRepository.save(user);
        postRepository.save(post);

    }

    @Transactional
    public void addReplyToComment(Long commentId, String username, String content){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Comment comment = commentRepository.findById(commentId).orElseThrow(NoSuchCommentException::new);

        Comment reply = new Comment (user, comment.getPost(), comment, content);
        userRepository.save(user);
        commentRepository.save(comment);
        commentRepository.save(reply);

    }



}
