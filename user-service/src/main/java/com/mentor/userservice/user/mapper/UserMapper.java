package com.mentor.userservice.user.mapper;

import com.mentor.userservice.user.dto.UserResponse;
import com.mentor.userservice.user.entity.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserResponse toResponse(UserAccount user);
}
