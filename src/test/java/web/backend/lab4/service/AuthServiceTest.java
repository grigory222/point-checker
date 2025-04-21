package web.backend.lab4.service;

import jakarta.ws.rs.core.NewCookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import web.backend.lab4.auth.JwtProvider;
import web.backend.lab4.auth.PasswordHasher;
import web.backend.lab4.dao.UserDAO;
import web.backend.lab4.dto.ErrorDTO;
import web.backend.lab4.dto.UserDTO;
import web.backend.lab4.entity.UserEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private UserDTO testUserDTO;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO("testUser", "testPassword");
        testUserEntity = UserEntity.builder()
                .id(1L)
                .username("testUser")
                .password(PasswordHasher.hashPassword("testPassword".toCharArray()))
                .build();
    }

    @Test
    void signup_WhenUserDoesNotExist_ShouldSuccessfullyAddUser() {
        when(userDAO.getUserByUsername(testUserDTO.getUsername())).thenReturn(Optional.empty());

        Optional<ErrorDTO> result = authService.signup(testUserDTO);

        assertTrue(result.isEmpty());
        verify(userDAO).addNewUser(any(UserEntity.class));
    }

    @Test
    void signup_WhenUserExists_ShouldReturnError() {
        when(userDAO.getUserByUsername(testUserDTO.getUsername())).thenReturn(Optional.of(testUserEntity));

        Optional<ErrorDTO> result = authService.signup(testUserDTO);

        assertTrue(result.isPresent());
        assertEquals("User with this name already exists", result.get().getError());
        verify(userDAO, never()).addNewUser(any(UserEntity.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        when(userDAO.getUserByUsername(testUserDTO.getUsername())).thenReturn(Optional.of(testUserEntity));
        when(jwtProvider.generateRefreshToken(anyString(), anyLong())).thenReturn("refreshToken");
        when(jwtProvider.generateAccessToken(anyString(), anyLong())).thenReturn("accessToken");

        Optional<AuthService.AuthResponse> result = authService.login(testUserDTO);

        assertTrue(result.isPresent());
        assertNotNull(result.get().accessTokenCookie());
        assertNotNull(result.get().refreshTokenCookie());
    }

    @Test
    void login_WithInvalidUsername_ShouldReturnEmpty() {
        when(userDAO.getUserByUsername(testUserDTO.getUsername())).thenReturn(Optional.empty());

        Optional<AuthService.AuthResponse> result = authService.login(testUserDTO);

        assertTrue(result.isEmpty());
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnEmpty() {
        UserDTO wrongPasswordDTO = new UserDTO("testUser", "wrongPassword");
        when(userDAO.getUserByUsername(wrongPasswordDTO.getUsername())).thenReturn(Optional.of(testUserEntity));

        Optional<AuthService.AuthResponse> result = authService.login(wrongPasswordDTO);

        assertTrue(result.isEmpty());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAccessToken() {
        String validRefreshToken = "validRefreshToken";
        String expectedUsername = "testUser";
        Long expectedUserId = 1L;
        String expectedToken = "newAccessToken";

        when(jwtProvider.getUsernameFromToken(validRefreshToken)).thenReturn(expectedUsername);
        when(jwtProvider.getUserIdFromToken(validRefreshToken)).thenReturn(expectedUserId);
        when(jwtProvider.generateAccessToken(expectedUsername, expectedUserId))
                .thenReturn(expectedToken);

        Optional<AuthService.AuthResponse> result = authService.refreshToken(validRefreshToken);

        assertTrue(result.isPresent());

        NewCookie accessCookie = result.get().accessTokenCookie();
        assertNotNull(accessCookie);
        assertEquals("access_token", accessCookie.getName());
        assertEquals(expectedToken, accessCookie.getValue());
        assertEquals("/", accessCookie.getPath());
        assertTrue(accessCookie.isHttpOnly());

        assertNull(result.get().refreshTokenCookie());
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldReturnEmpty() {
        String invalidRefreshToken = "invalidRefreshToken";
        when(jwtProvider.getUsernameFromToken(invalidRefreshToken)).thenThrow(new RuntimeException("Invalid token"));

        Optional<AuthService.AuthResponse> result = authService.refreshToken(invalidRefreshToken);

        assertTrue(result.isEmpty());
    }

    @Test
    void logout_ShouldReturnCookiesWithZeroMaxAge() {
        AuthService.AuthResponse response = authService.logout();

        assertEquals(0, response.accessTokenCookie().getMaxAge());
        assertEquals(0, response.refreshTokenCookie().getMaxAge());
        assertEquals("", response.accessTokenCookie().getValue());
        assertEquals("", response.refreshTokenCookie().getValue());
    }
}