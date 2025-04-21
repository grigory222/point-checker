package web.backend.lab4.service;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import web.backend.lab4.auth.JwtProvider;
import web.backend.lab4.dao.ResultDAO;
import web.backend.lab4.dao.UserDAO;
import web.backend.lab4.dto.ErrorDTO;
import web.backend.lab4.dto.PointDTO;
import web.backend.lab4.dto.ResultDTO;
import web.backend.lab4.entity.ResultEntity;
import web.backend.lab4.entity.UserEntity;
import web.backend.lab4.util.Calculator;

import java.util.List;
import java.util.Optional;
import jakarta.ejb.Stateless;

@Stateless
@Slf4j
public class PointService {
    @EJB
    private UserDAO userDAO;

    @EJB
    private ResultDAO resultDAO;

    @Inject
    private Calculator calculator;

    public Optional<ResultDTO> addPoint(PointDTO pointDTO, String username) {
        Optional<UserEntity> userOptional = userDAO.getUserByUsername(username);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        UserEntity user = userOptional.get();
        boolean result = calculator.calculate(pointDTO.getX(), pointDTO.getY(), pointDTO.getR());
        ResultEntity entity = ResultEntity.builder()
                .x(pointDTO.getX())
                .y(pointDTO.getY())
                .r(pointDTO.getR())
                .user(user)
                .result(result)
                .build();

        try {
            resultDAO.addNewResult(entity);
            return Optional.of(ResultDTO.of(entity.isResult()));
        } catch (Exception e) {
            log.error("Error adding point", e);
            return Optional.empty();
        }
    }

    public Optional<List<PointDTO>> getPoints(Long userId) {
        return Optional.of(resultDAO
                .getResultsByUserId(userId)
                .stream()
                .map(PointDTO::fromEntity)
                .toList());
    }
}