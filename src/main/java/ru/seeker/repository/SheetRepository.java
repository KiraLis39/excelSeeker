package ru.seeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.seeker.entity.Sheet;

import java.util.UUID;

@Repository
public interface SheetRepository extends JpaRepository<Sheet, UUID>, JpaSpecificationExecutor<Sheet> {

//    @Query(value = """
//            SELECT CAST(e.event_id AS varchar) FROM events e
//            WHERE NOW() < (e.event_end_date - INTERVAL '3 day')""", nativeQuery = true)

    @Modifying
    void deleteAllByDocUuid(UUID docUuid);

    @Modifying
    void deleteAllByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    long countByDocUuid(UUID docUuid);

    @Query(value = "select sh.docUuid from Sheet sh where sh.uuid = :sheetUuid")
    UUID getDocUuidByUuid(UUID sheetUuid);

    @Modifying
    void deleteAllBySheetNameLike(String sheetName);
}
