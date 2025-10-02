package com.cdc.ots_auth_service.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cdc.ots_auth_service.entity.User;
import com.cdc.ots_auth_service.repository.UserRepository;
import com.cdc.ots_auth_service.exception.EmailAlreadyExistsException;
import com.cdc.ots_auth_service.security.JwtService;
import com.cdc.ots_auth_service.security.KmsService;
import com.cdc.ots_auth_service.security.userdetails.CustomUserDetails;

@Service
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final KmsService kmsService;
    private final JwtService jwtService;
    private final MessageSource messageSource;

    public UserService(UserRepository userRepository, KmsService kmsService, JwtService jwtService, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.kmsService = kmsService;
        this.jwtService = jwtService;
        this.messageSource = messageSource;
    }

    public void register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("error.email.exists");
        }

        String encryptedPassword = kmsService.encrypt(password);

        User user = new User();
        user.setEmail(email);
        user.setEncryptedPassword(encryptedPassword);

        userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                                    .orElseThrow(() -> new UsernameNotFoundException(getMessage("error.user.notfound", email)));

        String decryptedPassword = kmsService.decrypt(user.getEncryptedPassword());

        if (!decryptedPassword.equals(password)) {
            throw new BadCredentialsException(getMessage("error.credentials.invalid"));
        }

        return jwtService.generateToken(user);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                            .orElseThrow(() -> new UsernameNotFoundException(getMessage("error.user.notfound", email)));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                            .map(CustomUserDetails::new)
                            .orElseThrow(() -> new UsernameNotFoundException(getMessage("error.user.notfound", email)));
    }

    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
