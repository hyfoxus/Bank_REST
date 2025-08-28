package com.gnemirko.bank_rest.service;

import com.gnemirko.bank_rest.dto.CreateUserRequest;
import com.gnemirko.bank_rest.entity.Role;
import com.gnemirko.bank_rest.entity.User;
import com.gnemirko.bank_rest.exception.UserNameAlreadyExistsException;
import com.gnemirko.bank_rest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    @Transactional
    @Override
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByName(request.name())) {
            throw new UserNameAlreadyExistsException(request.name());
        }

        Role role = Role.valueOf(request.role());

        User user = User.builder()
                .name(request.name())
                .passwordHash(encoder.encode(request.password()))
                .role(role)
                .build();

        return userRepository.save(user);
    }
}
