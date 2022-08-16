package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;

import java.util.List;

public interface PurchaseRepositoryCustom {

    List<ForListInfoResponseDto> purchaseList(User me, Post post, Long cursorId, int size);

}
