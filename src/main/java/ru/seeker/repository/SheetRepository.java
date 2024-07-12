package ru.seeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import ru.seeker.entity.Sheet;

import java.time.ZonedDateTime;
import java.util.UUID;

@Repository
public interface SheetRepository extends JpaRepository<Sheet, UUID>, JpaSpecificationExecutor<Sheet> {

//    @Query(value = """
//            SELECT CAST(e.event_id AS varchar) FROM events e
//            WHERE NOW() < (e.event_end_date - INTERVAL '3 day')""", nativeQuery = true)

    @Modifying
    void deleteAllByDocNameIgnoreCaseAndParsedDate(String docName, ZonedDateTime parsedDate);

    @Modifying
    void deleteAllBySheetNameIgnoreCaseAndParsedDate(String sheetName, ZonedDateTime parsedDate);

    boolean existsByDocNameIgnoreCaseAndParsedDate(String docName, ZonedDateTime parsedDate);
}
