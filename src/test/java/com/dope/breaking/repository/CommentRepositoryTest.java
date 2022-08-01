package com.dope.breaking.repository;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.comment.CommentLike;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.comment.CommentResponseDto;
import com.dope.breaking.dto.comment.SearchCommentConditionDto;
import com.dope.breaking.service.CommentTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CommentRepositoryTest {

    @Autowired CommentRepository commentRepository;
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired CommentLikeRepository commentLikeRepository;
    @Autowired EntityManager em;

    @DisplayName("댓글에는 커서 페이지네이션이 적용된다.")
    @Test
    void commentPagination() {

        User me = new User();
        userRepository.save(me);

        Post post = new Post();
        postRepository.save(post);

        for(int i=0;i<10;i++) {
            Comment comment = new Comment(me, post, "댓글"+i);
            commentRepository.save(comment);
        }
        em.flush();
        em.clear();

        SearchCommentConditionDto searchCommentConditionDto = SearchCommentConditionDto.builder()
            .targetId(post.getId())
            .targetType(CommentTargetType.POST)
            .size(4L)
            .cursorId(null)
            .build();

        List<CommentResponseDto> content1 = commentRepository.searchCommentList(me, searchCommentConditionDto);
        searchCommentConditionDto.setCursorId(content1.get(content1.size() - 1).getCommentId());
        List<CommentResponseDto> content2 = commentRepository.searchCommentList(me, searchCommentConditionDto);
        searchCommentConditionDto.setCursorId(content2.get(content2.size() - 1).getCommentId());
        List<CommentResponseDto> content3 = commentRepository.searchCommentList(me, searchCommentConditionDto);

        assertEquals(4, content1.size());
        assertEquals(4, content2.size());
        assertEquals(2, content3.size(), ()->"마지막 페지이는 2개가 나온다.");
    }

    @DisplayName("내가 좋아요한 게시글은, isLiked 필드가 true로 반환된다.")
    @Test
    void CheckIsLikeFieldFromCommentList() {

        User me = new User();
        userRepository.save(me);

        Post post = new Post();
        postRepository.save(post);

        Comment comment = new Comment(me, post, "댓글");
        commentRepository.save(comment);

        CommentLike commentLike = new CommentLike(me, comment);
        commentLikeRepository.save(commentLike);

        em.flush();
        em.clear();

        SearchCommentConditionDto searchCommentConditionDto = SearchCommentConditionDto.builder()
                .targetId(post.getId())
                .targetType(CommentTargetType.POST)
                .size(1L)
                .cursorId(null)
                .build();

        List<CommentResponseDto> content = commentRepository.searchCommentList(me, searchCommentConditionDto);

        assertTrue(content.get(0).getIsLiked());
    }

    @DisplayName("대댓글에는 커서 페이지네이션이 적용된다.")
    @Test
    void replyCommentPagination() {

        User me = new User();
        userRepository.save(me);

        Post post = new Post();
        postRepository.save(post);

        Comment parentComment = new Comment(me, post, "부모댓글");
        commentRepository.save(parentComment);

        for(int i=0;i<10;i++) {
            Comment comment = new Comment(me, null, parentComment, "자식댓글"+i);
            commentRepository.save(comment);
        }

        em.flush();

        SearchCommentConditionDto searchCommentConditionDto = SearchCommentConditionDto.builder()
                .targetId(parentComment.getId())
                .targetType(CommentTargetType.COMMENT)
                .size(4L)
                .cursorId(null)
                .build();

        List<CommentResponseDto> content1 = commentRepository.searchCommentList(me, searchCommentConditionDto);
        searchCommentConditionDto.setCursorId(content1.get(content1.size() - 1).getCommentId());
        List<CommentResponseDto> content2 = commentRepository.searchCommentList(me, searchCommentConditionDto);
        searchCommentConditionDto.setCursorId(content2.get(content2.size() - 1).getCommentId());
        List<CommentResponseDto> content3 = commentRepository.searchCommentList(me, searchCommentConditionDto);

        assertEquals(4, content1.size());
        assertEquals(4, content2.size());
        assertEquals(2, content3.size(), ()->"마지막 페지이는 2개가 나온다.");
    }
}