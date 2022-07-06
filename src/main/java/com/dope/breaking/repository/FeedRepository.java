package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Post, Long>, FeedRepositoryCustom {
}
