package com.cdc.ots_auth_service.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cdc.ots_auth_service.entity.User;
import com.cdc.ots_auth_service.repository.UserRepository;
import com.cdc.ots_auth_service.security.JwtService;
import com.cdc.ots_auth_service.security.KmsService;
import com.cdc.ots_auth_service.security.userdetails.CustomUserDetails;

@Service
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final KmsService kmsService;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, KmsService kmsService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.kmsService = kmsService;
        this.jwtService = jwtService;
    }

    public void register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        String encryptedPassword = kmsService.encrypt(password);

        User user = new User();
        user.setEmail(email);
        user.setEncryptedPassword(encryptedPassword);

        userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                                    .orElseThrow(() -> new RuntimeException("User not found"));

        String decryptedPassword = kmsService.decrypt(user.getEncryptedPassword());

        if (!decryptedPassword.equals(password)) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(user.getEmail());
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                            .map(CustomUserDetails::new)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
