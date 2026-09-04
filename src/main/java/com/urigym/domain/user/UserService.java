package com.urigym.domain.user;

import com.urigym.common.exception.ResourceNotFoundException;
import com.urigym.common.exception.DuplicateResourceException;
import com.urigym.domain.user.entity.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /** Only sets fields present in the request — lets the profile form and the
     *  notification-settings form share this endpoint without clobbering each other. */
    @Transactional
    public User updateUser(UUID id, UserUpdateRequest request) {
        User user = getUserById(id);

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            // Editing the number outside the SMS-verify flow means it's unverified again.
            user.setPhone(request.getPhone());
            user.setPhoneVerified(false);
        }
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getNotifyAnnouncements() != null) user.setNotifyAnnouncements(request.getNotifyAnnouncements());
        if (request.getNotifyMessages() != null) user.setNotifyMessages(request.getNotifyMessages());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        userRepository.delete(getUserById(id));
    }

    @Transactional
    public User changeRole(UUID id, AppRole role) {
        User user = getUserById(id);
        user.setRole(role);
        return userRepository.save(user);
    }

    public Page<User> getUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrPhoneContaining(
                keyword, keyword, keyword, pageable);
    }

    @Transactional
    public void updatePassword(UUID id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID id, String currentPassword, String newPassword) {
        User user = getUserById(id);

        if (user.getPassword() == null) {
            throw new IllegalArgumentException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        updatePassword(id, newPassword);
    }
}
