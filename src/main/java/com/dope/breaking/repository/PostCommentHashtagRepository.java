package com.dope.breaking.repository;

import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.PostCommentHashtag;
import com.dope.breaking.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentHashtagRepository extends JpaRepository<PostCommentHashtag, Long> {

    boolean existsByPostAndHashtag(Post post, Hashtag hashtag);


    boolean existsByHashtag(Hashtag hashtag);

    void deleteAllByPost(Post post);

    List<PostCommentHashtag> findAllByPost(Post post);

}
