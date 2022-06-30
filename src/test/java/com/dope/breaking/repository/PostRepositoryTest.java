package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PostRepositoryTest {
    @Autowired PostRepository postRepository;
    @Autowired EntityManager em;

    @Test
    void create_a_post() {
        Post post = new Post();

        postRepository.save(post);
        em.flush();
        em.clear();
        Post foundPost = postRepository.findById(post.getId()).get();

        assertEquals(post.getId(), foundPost.getId());
    }


}