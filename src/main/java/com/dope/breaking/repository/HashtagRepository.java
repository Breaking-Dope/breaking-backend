package com.dope.breaking.repository;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    boolean existsByComment(Comment comment);

    void deleteAllByPost(Post post);

    void deleteAllByComment(Comment comment);

    List<Hashtag> findAllByPost(Post post);

}
