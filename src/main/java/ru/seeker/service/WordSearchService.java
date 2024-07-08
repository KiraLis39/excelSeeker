package ru.seeker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WordSearchService {
//    private final ApplicationProperties props;
//    private final HttpService httpService;

    // тонкая настройка! не трогать без согласований:
//    @Transactional
//    public Page<EventDTO> findAllWithStatusesAndFilter(
//            Set<EventStatus> eventStatuses,
//            String projectIdOrSlug,
//            String eventFormat,
//            String subscribesStartDate,
//            String subscribesEndDate,
//            String eventTitle,
//            Boolean hasNotModeratedRequests,
//            String accessToken,
//            short page,
//            short count
//    ) {
//        boolean projectIdIsSet = false;
//        boolean projectSlugIsSet = false;
//        boolean hasNotModeratedRequestsIsSet = hasNotModeratedRequests != null;
//
//        // check projectId:
//        Integer projectId = null;
//        if (projectIdOrSlug != null && !projectIdOrSlug.isBlank()) {
//            try {
//                projectId = Integer.parseInt(projectIdOrSlug.trim());
//                projectIdIsSet = true;
//            } catch (NullPointerException | NumberFormatException e) {
//                projectSlugIsSet = true;
//            }
//        }
//
//        try {
//            // check subscribesStartDate and registrationEndDate:
//            ZonedDateTime subscribesStartDateZoned = (subscribesStartDate == null || subscribesStartDate.isBlank())
//                    ? ZonedDateTime.now().minus(1, ChronoUnit.CENTURIES) : ZonedDateTime.parse(subscribesStartDate.trim());
//            ZonedDateTime subscribesEndDateZoned = (subscribesEndDate == null || subscribesEndDate.isBlank())
//                    ? ZonedDateTime.now().plus(1, ChronoUnit.CENTURIES) : ZonedDateTime.parse(subscribesEndDate.trim());
//            if (eventTitle != null) {
//                eventTitle = eventTitle.toLowerCase().trim();
//            }
//            log.info("Выполняется поиск по фильтрам...");
//            Page<Event> result = eventRepository.findAllByFilter(
//                    eventStatuses,
//                    eventFormat == null ? null : eventFormat.trim(),
//                    subscribesStartDateZoned,
//                    subscribesEndDateZoned,
//                    projectIdIsSet,
//                    projectId,
//                    projectSlugIsSet,
//                    projectIdOrSlug,
//                    hasNotModeratedRequestsIsSet,
//                    hasNotModeratedRequests,
//                    eventTitle,
//                    Pageable.ofSize(count).withPage(page));
//            // временный код, исправляющий null в поле eventStamp (после его добавления в таблицу БД):
//            result.getContent().forEach(event -> {
//                if (event.getEventStamp() == null) {
//                    event.setEventStamp(UUID.randomUUID());
//                    eventRepository.save(event);
//                }
//                if (event.getAuthor() != null && (event.getAuthorName() == null || event.getAuthorSurname() == null)) {
//                    findAndUpdateAuthorByUUID(event);
//                    eventRepository.save(event);
//                }
//            });
//            return eventMapper.toEventDto(result, accessToken);
//        } catch (Exception ex) {
//            log.warn("Ошибка обработки /list -> findAllWithStatusesAndFilter(): {}",
//                    ExceptionUtils.getFullExceptionMessage(ex));
//            throw new GlobalServiceException(ErrorMessages.UNIVERSAL_ERROR_MESSAGE_TEMPLATE, ex);
//        }
//    }
}
