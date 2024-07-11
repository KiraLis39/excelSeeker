package ru.seeker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import ru.seeker.entity.Sheet;

import java.util.UUID;

@Repository
public interface SheetRepository extends JpaRepository<Sheet, UUID>, JpaSpecificationExecutor<Sheet> {

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

//    @Query(value = """
//            from Sheet pr left join pr.items as item
//            where LOWER(item.title) like %:text%
//            or LOWER(item.description) like %:text%
//            or LOWER(item.category) like %:text%
//            or LOWER(item.sku) like %:text%
//            or LOWER(item.excerpt) like %:text%""")
//    Page<Sheet> findAllByText(String text, Pageable page);

    @Modifying
    void deleteAllByDocNameIgnoreCase(String docName);

    @Modifying
    void deleteAllBySheetNameIgnoreCase(String sheetName);

    boolean existsByDocName(String docName);
}
