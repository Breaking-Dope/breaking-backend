package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final PostRepository postRepository;

    public Page<Post> searchFeed(int size, int page) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        return postRepository.findAll(pageRequest);
    }

    public Page<Post> searchFeed(int size, int page, SortFilter sortFilter) {
        // 미구현
        // QueryDSL 적용하여 리팩토링합니다.
        return null;
    }

}
