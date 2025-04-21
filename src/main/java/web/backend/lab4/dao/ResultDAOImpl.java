package web.backend.lab4.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import web.backend.lab4.entity.ResultEntity;
import web.backend.lab4.entity.UserEntity;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the ResultDAO interface using JPA (Java Persistence API).
 * Handles database operations for ResultEntity objects.
 */
@Stateless
public class ResultDAOImpl implements ResultDAO {
    @PersistenceContext(name="idk")
    private EntityManager entityManager;

    @Override
    public void addNewResult(ResultEntity result) {
        entityManager.persist(result);
        entityManager.flush();
    }

    public List<ResultEntity> getResultsByUserId(Long userId) {
        Optional<UserEntity> user = findUserByUserId(userId);
        return entityManager.createQuery("SELECT p FROM ResultEntity p WHERE p.user.id = :userId", ResultEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public Optional<UserEntity> findUserByUserId(Long userId) {
        UserEntity user = entityManager.find(UserEntity.class, userId);
        return Optional.ofNullable(user);
    }
}
