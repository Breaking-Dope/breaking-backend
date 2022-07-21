package com.dope.breaking.repository;

import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.PostHashtag;
import com.dope.breaking.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {

    boolean existsByPostAndHashtag(Post post, Hashtag hashtag);


    boolean existsByHashtag(Hashtag hashtag);

    void deleteAllByPost(Post post);

    List<PostHashtag> findAllByPost(Post post);

}
