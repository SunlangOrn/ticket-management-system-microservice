package com.mentor.userservice.service;

import java.util.UUID;

import com.mentor.shared.exception.HttpException;
import com.mentor.userservice.dto.UserResponse;
import com.mentor.userservice.exception.UserException;
import com.mentor.userservice.mapper.UserMapper;
import com.mentor.userservice.repository.UserRepository;
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
