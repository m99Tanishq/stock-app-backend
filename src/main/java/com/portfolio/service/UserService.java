package com.portfolio.service;

import com.portfolio.domain.User;
import com.portfolio.dto.UserDTO;
import com.portfolio.exception.CustomException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new CustomException("Username already exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new CustomException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDTO(user);
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}