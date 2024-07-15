package ru.seeker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.seeker.entity.Item;

import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID>, JpaSpecificationExecutor<Item> {

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

    @Query(value = """
            SELECT * FROM (
                SELECT DISTINCT ON(itm.sku) sts.parsed_date date, itm.*
                FROM public.items itm LEFT JOIN public.sheets sts ON itm.sheet = sts.uuid
                ORDER BY itm.sku, sts.parsed_date DESC) t
            WHERE LOWER(title) like CONCAT('%', :#{#text}, '%')
                OR LOWER(model) like CONCAT('%', :#{#text}, '%')
                OR LOWER(sku) like CONCAT('%', :#{#text}, '%')""",
            nativeQuery = true)
    Page<Item> findAllByText(@Param("text") String text, Pageable page);

    // OR LOWER(description) like CONCAT('%', :#{#text}, '%')
    // OR LOWER(excerpt) like CONCAT('%', :#{#text}, '%')
    // OR LOWER(category) like CONCAT('%', :#{#text}, '%')
}
