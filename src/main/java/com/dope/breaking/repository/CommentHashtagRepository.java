package com.dope.breaking.repository;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.hashtag.CommentHashtag;
import com.dope.breaking.domain.hashtag.Hashtag;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentHashtagRepository extends JpaRepository<CommentHashtag, Long> {
    boolean existsByHashtag(Hashtag hashtag);
}
