package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Bookmark;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.bookmark.AlreadyBookmarkedException;
import com.dope.breaking.exception.bookmark.AlreadyUnbookmarkedException;
import com.dope.breaking.repository.BookmarkRepository;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class BookmarkServiceTest {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private BookmarkRepository bookmarkRepository;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;


    @DisplayName("유저가 게시글을 북마크를 시도할 시, 정상적으로 처리된다.")
    @Test
    void bookmarkPost() {
        //Given
        User user = User.builder().username("123").build();
        Post post1 = Post.builder().build();
        Post post2 = Post.builder().build();

        userRepository.save(user);
        postRepository.save(post1);
        postRepository.save(post2);


        //When
        bookmarkService.bookmarkPost(user.getUsername(), post1.getId());
        bookmarkService.bookmarkPost(user.getUsername(), post2.getId());

        //Then
        Assertions.assertThat(bookmarkRepository.existsByUserAndPost(user, post1)).isTrue();
        Assertions.assertThat(bookmarkRepository.existsByUserAndPost(user, post2)).isTrue();

    }

    @DisplayName("이미 북마크한 유저가 북마크를 시도하려 할 시, 예외가 반환된다")
    @Test
    void AlreadyBookmarkPost() {
        //Given
        User user = User.builder().username("123").build();
        Post post1 = Post.builder().build();
        Post post2 = Post.builder().build();

        userRepository.save(user);
        postRepository.save(post1);
        postRepository.save(post2);
        Bookmark bookmark1 = Bookmark.builder()
                .post(post1)
                .user(user).build();
        bookmarkRepository.save(bookmark1);
        Bookmark bookmark2 = Bookmark.builder()
                .post(post2)
                .user(user).build();
        bookmarkRepository.save(bookmark2);

        //When
        org.junit.jupiter.api.Assertions.assertThrows(AlreadyBookmarkedException.class, () -> bookmarkService.bookmarkPost(user.getUsername(), post1.getId()));
        org.junit.jupiter.api.Assertions.assertThrows(AlreadyBookmarkedException.class, () -> bookmarkService.bookmarkPost(user.getUsername(), post2.getId()));

    }

        @DisplayName("유저가 게시글의 북마크를 해제하려 할 시, 정상적으로 처리된다.")
        @Test
        void unbookmarkPost () {
            //Given
            User user = User.builder().username("123").build();
            Post post1 = Post.builder().build();
            Post post2 = Post.builder().build();

            userRepository.save(user);
            postRepository.save(post1);
            postRepository.save(post2);

            Bookmark bookmark1 = Bookmark.builder()
                    .post(post1)
                    .user(user).build();
            bookmarkRepository.save(bookmark1);
            Bookmark bookmark2 = Bookmark.builder()
                    .post(post2)
                    .user(user).build();
            bookmarkRepository.save(bookmark2);

            //When
            bookmarkService.unbookmarkPost(user.getUsername(), post1.getId());
            bookmarkService.unbookmarkPost(user.getUsername(), post2.getId());

            //Then
            Assertions.assertThat(bookmarkRepository.existsByUserAndPost(user, post1)).isFalse();
            Assertions.assertThat(bookmarkRepository.existsByUserAndPost(user, post2)).isFalse();
        }

    @DisplayName("이미 북마크를 해제한 유저가 북마크 해제를 시도하려 할 시, 예외가 반환된다")
    @Test
    void AlreadyUnbookmarkPost() {
        //Given
        User user = User.builder().username("123").build();
        Post post1 = Post.builder().build();
        Post post2 = Post.builder().build();

        userRepository.save(user);
        postRepository.save(post1);
        postRepository.save(post2);

        //When
        org.junit.jupiter.api.Assertions.assertThrows(AlreadyUnbookmarkedException.class, () -> bookmarkService.unbookmarkPost(user.getUsername(), post1.getId()));
        org.junit.jupiter.api.Assertions.assertThrows(AlreadyUnbookmarkedException.class, () -> bookmarkService.unbookmarkPost(user.getUsername(), post2.getId()));

    }
    }