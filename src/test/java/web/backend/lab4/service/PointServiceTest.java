package web.backend.lab4.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import web.backend.lab4.dao.ResultDAO;
import web.backend.lab4.dao.UserDAO;
import web.backend.lab4.dto.PointDTO;
import web.backend.lab4.dto.ResultDTO;
import web.backend.lab4.entity.ResultEntity;
import web.backend.lab4.entity.UserEntity;
import web.backend.lab4.util.Calculator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private ResultDAO resultDAO;

    @Mock
    private Calculator calculator;

    @InjectMocks
    private PointService pointService;

    private final UserEntity testUser = UserEntity.builder()
            .id(1L)
            .username("testUser")
            .build();

    // Теперь явно указываем result в конструкторе
    private final PointDTO testPoint = PointDTO.builder()
            .x(1)
            .y(2.0)
            .r(3)
            .result(false) // Значение по умолчанию
            .build();

    @BeforeEach
    void setUp() {
    }

    @Test
    void addPoint_WithValidUser_ShouldReturnResult() {
        // Arrange
        when(userDAO.getUserByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(calculator.calculate(1, 2.0, 3)).thenReturn(true);

        // Мокаем void-метод с помощью doAnswer
        doAnswer(inv -> {
            ResultEntity e = inv.getArgument(0);
            e.setId(1L); // Эмулируем сохранение в БД
            return null;
        }).when(resultDAO).addNewResult(any(ResultEntity.class));

        // Act
        Optional<ResultDTO> result = pointService.addPoint(testPoint, "testUser");

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isResult());
        verify(resultDAO).addNewResult(argThat(entity ->
                entity.getX() == 1 &&
                        entity.getY() == 2.0 &&
                        entity.getR() == 3 &&
                        entity.getUser().equals(testUser)
        ));
    }

    @Test
    void getPoints_ShouldReturnCorrectResultField() {
        // Arrange
        ResultEntity entity = ResultEntity.builder()
                .x(1).y(2.0).r(3).result(true).user(testUser).build();

        when(resultDAO.getResultsByUserId(1L)).thenReturn(List.of(entity));

        // Act
        Optional<List<PointDTO>> result = pointService.getPoints(1L);

        // Assert
        assertTrue(result.isPresent());
        PointDTO dto = result.get().get(0);
        assertTrue(dto.isResult()); // Проверяем что result правильно маппится
    }

    @Test
    void addPoint_ShouldIgnoreInputResultField() {
        // Arrange
        PointDTO pointWithWrongResult = PointDTO.builder()
                .x(1).y(2.0).r(3).result(true).build(); // Указываем неверный результат

        when(userDAO.getUserByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(calculator.calculate(1, 2.0, 3)).thenReturn(false); // Но калькулятор вернет false

        // Act
        Optional<ResultDTO> result = pointService.addPoint(pointWithWrongResult, "testUser");

        // Assert
        assertTrue(result.isPresent());
        assertFalse(result.get().isResult()); // Должен быть результат расчета, а не входного DTO
    }
}