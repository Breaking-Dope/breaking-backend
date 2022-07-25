package com.dope.breaking.repository;

import com.dope.breaking.domain.hashtag.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Boolean existsByHashtag(String hashtag);

    Hashtag findByHashtag(String hashtag);
}
