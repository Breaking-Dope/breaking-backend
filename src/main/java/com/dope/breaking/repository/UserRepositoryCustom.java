package com.dope.breaking.repository;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.SearchUserResponseDto;

import java.util.List;

public interface UserRepositoryCustom {

    List<SearchUserResponseDto> searchUserBy(User me, String searchKeyword, User cursorUser, Long size);
}
