package ru.seeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.seeker.entity.PassStore;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PassRepository extends JpaRepository<PassStore, UUID>, JpaSpecificationExecutor<PassStore> {

    @Query(value = "select ps.passHash from PassStore ps where ps.login = :login")
    Optional<Integer> findPassHashByLogin(String login);

    @Modifying
    @Query(value = "update pass set pass_hash = :hash where login = :login", nativeQuery = true)
    void updatePasswordByLogin(String login, int hash);
}
