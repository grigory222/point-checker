package web.backend.lab4.service;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import web.backend.lab4.auth.JwtProvider;
import web.backend.lab4.auth.PasswordHasher;
import web.backend.lab4.dao.UserDAO;
import web.backend.lab4.dto.ErrorDTO;
import web.backend.lab4.dto.UserDTO;
import web.backend.lab4.entity.UserEntity;
import jakarta.ws.rs.core.NewCookie;

import java.util.Optional;
import jakarta.ejb.Stateless;

@Stateless
@Slf4j
public class AuthService {
    @EJB
    private UserDAO userDAO;

    @Inject
    private JwtProvider jwtProvider;

    public Optional<ErrorDTO> signup(@Valid UserDTO userDTO) {
        if (userDAO.getUserByUsername(userDTO.getUsername()).isPresent()) {
            return Optional.of(ErrorDTO.of("User with this name already exists"));
        }

        UserEntity user = UserEntity.builder()
                .username(userDTO.getUsername())
                .password(PasswordHasher.hashPassword(userDTO.getPassword().toCharArray()))
                .build();

        userDAO.addNewUser(user);
        log.info("Successfully added user: {}", user);
        return Optional.empty();
    }

    public Optional<AuthResponse> login(@Valid UserDTO userDTO) {
        Optional<UserEntity> userOptional = userDAO.getUserByUsername(userDTO.getUsername());
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            if (PasswordHasher.checkPassword(userDTO.getPassword().toCharArray(), user.getPassword())) {
                String refreshToken = jwtProvider.generateRefreshToken(user.getUsername(), user.getId());
                String accessToken = jwtProvider.generateAccessToken(user.getUsername(), user.getId());

                NewCookie refreshTokenCookie = createCookie("refresh_token", refreshToken, JwtProvider.REFRESH_TOKEN_EXPIRATION);
                NewCookie accessTokenCookie = createCookie("access_token", accessToken, JwtProvider.ACCESS_TOKEN_EXPIRATION);

                return Optional.of(new AuthResponse(accessTokenCookie, refreshTokenCookie));
            }
        }
        return Optional.empty();
    }

    public Optional<AuthResponse> refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Optional.empty();
        }

        try {
            String username = jwtProvider.getUsernameFromToken(refreshToken);
            Long userId = jwtProvider.getUserIdFromToken(refreshToken);
            String newAccessToken = jwtProvider.generateAccessToken(username, userId);
            NewCookie accessTokenCookie = createCookie("access_token", newAccessToken, JwtProvider.ACCESS_TOKEN_EXPIRATION);
            return Optional.of(new AuthResponse(accessTokenCookie, null));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public AuthResponse logout() {
        NewCookie accessTokenCookie = createCookie("access_token", "", 0);
        NewCookie refreshTokenCookie = createCookie("refresh_token", "", 0);
        return new AuthResponse(accessTokenCookie, refreshTokenCookie);
    }

    private NewCookie createCookie(String name, String value, int maxAge) {
        return new NewCookie.Builder(name)
                .value(value)
                .maxAge(maxAge)
                .path("/")
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.NONE)
                .secure(true)
                .build();
    }

    public record AuthResponse(NewCookie accessTokenCookie, NewCookie refreshTokenCookie) {

    }
}