package com.dope.breaking.service;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.hashtag.HashtagType;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.HashtagRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
class HashtagServiceTest {

    @Autowired
    HashtagService hashtagService;

    @Autowired
    HashtagRepository hashtagRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void savePostHashtag() {
        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        List<String> hashtag = new ArrayList<>();
        hashtag.add("hello1");
        hashtag.add("hello2");
        //When
        hashtagService.saveHashtag(hashtag, postId, HashtagType.POST);

        //Then

        Assertions.assertSame(hashtagRepository.findAllByPost(post).size(), 2);
        Assertions.assertSame(hashtagRepository.findAllByPost(post).get(0).getContent(), "hello1");
        Assertions.assertSame(hashtagRepository.findAllByPost(post).get(1).getContent(), "hello2");
    }

    @Test
    void modifyPostHashtag() {
        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        List<String> hashtags = new ArrayList<>();
        hashtags.add("hello1");
        hashtags.add("hello2");
        hashtagService.saveHashtag(hashtags, postId, HashtagType.POST);

        //When
        hashtags.clear();
        hashtags.add("hello1");
        hashtags.add("hello3");
        hashtagService.updateHashtag(hashtags, postId, HashtagType.POST);

        //Then
        Assertions.assertSame(hashtagRepository.findAllByPost(post).size(), 2);
        Assertions.assertSame(hashtagRepository.findAllByPost(post).get(0).getContent(), "hello1");
        Assertions.assertSame(hashtagRepository.findAllByPost(post).get(1).getContent(), "hello3");

    }

    @DisplayName("댓글 내에 해시태그가 존재할 경우, 해시태그가 정상 등록된다")
    @Transactional
    @Test
    void saveCommentHashtags() {

        //Given
        User user = new User();
        Post post = new Post();
        userRepository.save(user);
        postRepository.save(post);

        Comment comment = new Comment(user, post, "hi");
        commentRepository.save(comment);

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("hashtag1");
        hashtagList.add("hashtag2");

        //When
        hashtagService.saveHashtag(hashtagList, comment.getId(),HashtagType.COMMENT);

        //Then
        Assertions.assertEquals(2, hashtagRepository.findAll().size());

    }

    @DisplayName("댓글 내에 해시태그가 없을 경우, 해시태그가 등록되지 않는다")
    @Transactional
    @Test
    void saveNoHashtags() {

        //Given
        User user = new User();
        Post post = new Post();
        userRepository.save(user);
        postRepository.save(post);

        Comment comment = new Comment(user, post, "hi");
        commentRepository.save(comment);

        List<String> hashtagList = new ArrayList<>();

        //When
        hashtagService.saveHashtag(hashtagList, comment.getId(),HashtagType.COMMENT);

        //Then
        Assertions.assertEquals(0, hashtagRepository.findAll().size());

    }

    @DisplayName("댓글을 수정할 경우, 해시태그도 수정된다")
    @Transactional
    @Test
    void checkHashtagsWhenUpdateComment() {

        //Given
        User user = new User();
        Post post = new Post();
        userRepository.save(user);
        postRepository.save(post);

        Comment comment = new Comment(user, post, "hi");
        commentRepository.save(comment);

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("hashtag1");
        hashtagList.add("hashtag2");

        hashtagService.saveHashtag(hashtagList, comment.getId(),HashtagType.COMMENT);

        //When
        List<String> newHashtagList = new ArrayList<>();
        newHashtagList.add("newHashtag1");
        newHashtagList.add("newHashtag2");
        newHashtagList.add("newHashtag2");

        hashtagService.updateHashtag(newHashtagList, comment.getId(), HashtagType.COMMENT);

        //Then
        Assertions.assertEquals(3, hashtagRepository.findAll().size());

    }

    @DisplayName("댓글이 삭제될 경우, 해당하는 해시태그도 삭제된다")
    @Transactional
    @Test
    void checkHashtagsDeletedWhenCommentDeleted(){

        //Given
        User user = new User();
        Post post = new Post();
        userRepository.save(user);
        postRepository.save(post);

        Comment comment = new Comment(user, post, "hi");
        commentRepository.save(comment);

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("hashtag1");
        hashtagList.add("hashtag2");

        hashtagService.saveHashtag(hashtagList, comment.getId(),HashtagType.COMMENT);

        //When
        commentRepository.delete(comment);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertEquals(0, commentRepository.findAll().size());
        Assertions.assertEquals(0, hashtagRepository.findAll().size());

    }

    @DisplayName("댓글을 단 제보가 삭제될 경우, 댓글과 해당하는 해시태그도 삭제된다")
    @Transactional
    @Test
    void checkHashtagsDeletedWhenPostDeleted(){

        //Given
        User user = new User();
        Post post = new Post();
        userRepository.save(user);
        postRepository.save(post);

        Comment comment = new Comment(user, post, "hi");
        commentRepository.save(comment);

        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("hashtag1");
        hashtagList.add("hashtag2");

        hashtagService.saveHashtag(hashtagList, comment.getId(),HashtagType.COMMENT);

        //When
        postRepository.delete(post);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertEquals(0, commentRepository.findAll().size());
        Assertions.assertEquals(0, hashtagRepository.findAll().size());

    }

}