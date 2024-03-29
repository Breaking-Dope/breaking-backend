package com.dope.breaking.service;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.hashtag.HashtagType;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.comment.CommentResponseDto;
import com.dope.breaking.dto.comment.SearchCommentConditionDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.comment.NoSuchCommentException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.HashtagRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final HashtagService hashtagService;
    private final HashtagRepository hashtagRepository;

    @Transactional
    public Long addComment(Long postId, String username, String content, List<String> hashtagList) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        Comment comment = new Comment(user, post, content);

        commentRepository.save(comment);
        userRepository.save(user);
        postRepository.save(post);

        hashtagService.saveHashtag(hashtagList, comment.getId(), HashtagType.COMMENT);

        return comment.getId();

    }

    @Transactional
    public Long addReply(Long commentId, String username, String content, List<String> hashtagList) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Comment comment = commentRepository.findById(commentId).orElseThrow(NoSuchCommentException::new);

        Comment reply = new Comment(user, comment.getPost(), comment, content);
        userRepository.save(user);
        commentRepository.save(comment);
        commentRepository.save(reply);

        hashtagService.saveHashtag(hashtagList, reply.getId(), HashtagType.COMMENT);

        return reply.getId();

    }

    @Transactional
    public void updateCommentOrReply(String username, Long commentId, String content, List<String> hashtagList) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Comment comment = commentRepository.findById(commentId).orElseThrow(NoSuchCommentException::new);

        if (comment.getUser()!=user) {
            throw new NoPermissionException();
        }

        hashtagService.updateHashtag(hashtagList,commentId,HashtagType.COMMENT);

        comment.updateComment(content);

    }

    @Transactional
    public void deleteCommentOrReply(String username, Long commentId) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Comment comment = commentRepository.findById(commentId).orElseThrow(NoSuchCommentException::new);

        if (comment.getUser()!=user) {
            throw new NoPermissionException();
        }

        commentRepository.delete(comment);

    }

    public List<CommentResponseDto> getCommentList(SearchCommentConditionDto searchCommentConditionDto, String username, Long cursorId) {

        User me = null;
        if(username != null) {
            me = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        }

        Comment cursorComment = null;
        if(cursorId != null && cursorId != 0) {
            cursorComment = commentRepository.findById(cursorId).orElseThrow(NoSuchCommentException::new);
        }

        switch (searchCommentConditionDto.getTargetType()) {
            case POST:
                if(!postRepository.existsById(searchCommentConditionDto.getTargetId())) {
                    throw new NoSuchPostException();
                }
                break;

            case COMMENT:
                if(!commentRepository.existsById(searchCommentConditionDto.getTargetId())) {
                    throw new NoSuchCommentException();
                }
                break;
        }

        return commentRepository.searchCommentList(me, searchCommentConditionDto, cursorComment);
    }
}
