package com.example.logisquare_server.auth.service;

import com.example.logisquare_server.auth.dto.AuthUserResponse;
import com.example.logisquare_server.auth.dto.LoginRequest;
import com.example.logisquare_server.auth.dto.LoginResponse;
import com.example.logisquare_server.auth.exception.InvalidLoginException;
import com.example.logisquare_server.auth.token.JwtTokenProvider;
import com.example.logisquare_server.domain.user.User;
import com.example.logisquare_server.repository.UserRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (isBlank(request.username()) || isBlank(request.password())) {
            throw new InvalidLoginException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = userRepository.findByLoginId(request.username())
                .filter(foundUser -> Boolean.TRUE.equals(foundUser.getActive()))
                .filter(foundUser -> Objects.equals(foundUser.getPassword(), request.password()))
                .orElseThrow(() -> new InvalidLoginException("아이디 또는 비밀번호가 올바르지 않습니다."));

        String accessToken = jwtTokenProvider.createAccessToken(user);
        AuthUserResponse authUser = new AuthUserResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getRole()
        );

        return new LoginResponse(accessToken, authUser);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
