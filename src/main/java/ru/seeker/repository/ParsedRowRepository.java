package ru.seeker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.seeker.entity.ParsedRow;

@Repository
public interface ParsedRowRepository extends JpaRepository<ParsedRow, Long>, JpaSpecificationExecutor<ParsedRow> {

//    @Query(value = """
//            SELECT CAST(e.event_id AS varchar) FROM events e
//            WHERE NOW() < (e.event_end_date - INTERVAL '3 day')""", nativeQuery = true)

//    @Query("""
//            SELECT e FROM EventSubscribe e
//              WHERE e.subscribeStatus = :status
//              AND e.linkedEvent.id = :innerEventId
//              AND (:searchKey IS NULL or (lower(e.participant.companyName) like %:searchKey% ))
//              AND (:boolCategory = true or e.category = :category)
//              AND (:boolNomination = true or e.nomination = :nomination)""")

    //    @Query("""
//            from Event e
//            where e.eventStatus in :eventStatuses
//            and (:eventTitle is null or lower(e.eventName) like %:eventTitle%)
//            and (:hasNotModeratedRequestsIsSet = false
//                or ((:hasNotModeratedRequests = false and e.onModerationRequestsCount = 0)
//                    or (:hasNotModeratedRequests = true and e.onModerationRequestsCount > 0)))
//            and (e.eventFormat like :eventFormat or :eventFormat is null)
//            and (e.registrationEndDate is null or e.registrationEndDate <= :subscribesEndDateZoned)
//            and (e.registrationBeginDate is null or e.registrationBeginDate >= :subscribesStartDateZoned)
//            and ((:projectIdIsSet = false or e.eventProject.projectId = :projectId)
//                and (:projectSlugIsSet = false
//                or e.eventProject.projectSlug like :projectSlug
//                or e.eventProject.projectName like :projectSlug))""")
    @Query(value = "select pr from ParsedRow pr where LOWER(pr.rowData) like %:text%")
    Page<ParsedRow> findAllByText(String text, Pageable page);

    @Modifying
    void deleteAllByDocNameIgnoreCase(String docName);

    boolean existsByDocName(String docName);
}
