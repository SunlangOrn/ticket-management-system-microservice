package com.mentor.userservice.user.service;

import java.util.UUID;

import com.mentor.userservice.user.dto.UserResponse;
import com.mentor.userservice.user.exception.UserException;
import com.mentor.userservice.user.mapper.UserMapper;
import com.mentor.userservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(UserException::notFound);
    }
}
