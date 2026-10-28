package com.mentor.userservice.mapper;

import com.mentor.userservice.dto.UserResponse;
import com.mentor.userservice.entity.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserResponse toResponse(UserAccount user);
}
