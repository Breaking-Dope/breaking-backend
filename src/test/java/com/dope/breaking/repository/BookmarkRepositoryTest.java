package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Bookmark;
import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;


import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
public class BookmarkRepositoryTest {

    @Autowired BookmarkRepository bookmarkRepository;
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;

    @DisplayName("유저가 북마크를 한 게시물이면, true가 반한된다.")
    @Test
    void bookmarkExistByUser() {
        User user = new User();
        userRepository.save(user);
        Post post = new Post();
        postRepository.save(post);
        Bookmark bookmark = new Bookmark(user, post);
        bookmarkRepository.save(bookmark);

        assertTrue(bookmarkRepository.existsByUserAndPostId(user, post.getId()));
    }

    @DisplayName("특정 유저의 팔로잉 및 북마크 목록이 모두 삭제된다.")
    @Test
    void deleteAllBookmarkByUser() {

        User user = new User();
        userRepository.save(user);

        User otherUser = new User();
        userRepository.save(otherUser);

        Post post1 = Post.builder().build();
        Post post2 = Post.builder().build();
        postRepository.save(post1);
        postRepository.save(post2);

        bookmarkRepository.save(new Bookmark(user, post1));
        bookmarkRepository.save(new Bookmark(user, post2));
        bookmarkRepository.save(new Bookmark(otherUser, post1));

        Long preBookmarkCount = bookmarkRepository.count();

        bookmarkRepository.deleteAllByUser(user);

        Assertions.assertEquals(3, preBookmarkCount);
        Assertions.assertEquals(1, bookmarkRepository.count());
    }
}
