package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Bookmark;
import com.dope.breaking.domain.user.User;
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
}
