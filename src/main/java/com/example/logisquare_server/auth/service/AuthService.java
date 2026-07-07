package com.example.logisquare_server.auth.service;

import com.example.logisquare_server.auth.dto.AuthUserResponse;
import com.example.logisquare_server.auth.dto.CreateWorkerAccountRequest;
import com.example.logisquare_server.auth.dto.CreateWorkerAccountResponse;
import com.example.logisquare_server.auth.dto.LoginRequest;
import com.example.logisquare_server.auth.dto.LoginResponse;
import com.example.logisquare_server.auth.exception.DuplicateWorkerAccountException;
import com.example.logisquare_server.auth.exception.InvalidLoginException;
import com.example.logisquare_server.auth.token.JwtTokenProvider;
import com.example.logisquare_server.domain.user.User;
import com.example.logisquare_server.domain.user.UserRole;
import com.example.logisquare_server.domain.worker.Worker;
import com.example.logisquare_server.repository.UserRepository;
import com.example.logisquare_server.repository.WorkerRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String DEFAULT_WORKER_STATUS = "AVAILABLE";

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            WorkerRepository workerRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public CreateWorkerAccountResponse createWorkerAccount(CreateWorkerAccountRequest request) {
        validateCreateWorkerAccountRequest(request);

        if (userRepository.existsByLoginId(request.loginId())) {
            throw new DuplicateWorkerAccountException("이미 사용 중인 아이디입니다.");
        }
        if (workerRepository.existsByEmployeeNo(request.employeeNo())) {
            throw new DuplicateWorkerAccountException("이미 사용 중인 사번입니다.");
        }

        User user = userRepository.save(new User(
                request.loginId(),
                request.password(),
                request.name(),
                UserRole.USER,
                true
        ));
        Worker worker = workerRepository.save(new Worker(user, request.employeeNo(), DEFAULT_WORKER_STATUS));

        return new CreateWorkerAccountResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getRole(),
                worker.getId(),
                worker.getEmployeeNo(),
                worker.getStatus()
        );
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

    private void validateCreateWorkerAccountRequest(CreateWorkerAccountRequest request) {
        if (request == null
                || isBlank(request.loginId())
                || isBlank(request.password())
                || isBlank(request.name())
                || isBlank(request.employeeNo())) {
            throw new DuplicateWorkerAccountException("아이디, 비밀번호, 이름, 사번은 필수입니다.");
        }
    }
}
