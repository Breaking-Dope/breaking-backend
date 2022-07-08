package com.dope.breaking.repository;

import com.dope.breaking.domain.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media,Long> {

    Media findByPostIdAndMediaURL(Long postid, String url);

    List<Media> findAllByPostId(Long postid);
}
