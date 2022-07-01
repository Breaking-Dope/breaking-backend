package com.dope.breaking.repository;

import com.dope.breaking.domain.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media,Long> {
}
