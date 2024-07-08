package ru.seeker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seeker.dto.ParsedRowDTO;
import ru.seeker.entity.ParsedRow;
import ru.seeker.mapper.ParsedRowMapper;
import ru.seeker.repository.FilesStoryRepository;
import ru.seeker.repository.ParsedRowRepository;

import java.util.Collection;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ParsedRowService {
    private final ParsedRowRepository repository;
    private final FilesStoryRepository storyRepository;
    private final ParsedRowMapper mapper;

    @Transactional(readOnly = true)
    public Page<ParsedRowDTO> findAllByText(String text, int count, int page) {
        log.info("Поиск в базе по строке '{}'...", text.toLowerCase());
        Page<ParsedRow> found = repository.findAllByText(text.toLowerCase(), Pageable.ofSize(count).withPage(page));
        log.info("Найдено в базе совпадений по тексту {}: {} шт.", text, found.getContent().size());
        return mapper.toDto(found);
    }

    public ResponseEntity<HttpStatus> deleteAllDataByDocName(String docName) {
        boolean isExists = repository.existsByDocName(docName);
        if (!isExists) {
            log.info("Не найден на удаление документ с именем '{}'", docName);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.warn("Удаление данных документа '{}'...", docName);
        repository.deleteAllByDocNameIgnoreCase(docName);
        storyRepository.deleteByDocName(docName);
        log.warn("Документ '{}' был удалён из базы.", docName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void saveRows(Collection<ParsedRowDTO> toSave) {
        log.info("Сохранение в БД строк в количестве '{}'...", toSave.size());
        repository.saveAllAndFlush(mapper.toEntity(toSave));
    }
}
