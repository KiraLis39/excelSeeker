package ru.seeker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.seeker.dto.BaseWebModelDTO;
import ru.seeker.dto.ParsedRowDTO;
import ru.seeker.entity.ParsedRow;
import ru.seeker.mapper.ParsedRowMapper;
import ru.seeker.repository.FilesStoryRepository;
import ru.seeker.repository.ParsedRowRepository;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ParsedRowService {
    private final ParsedRowRepository repository;
    private final FilesStoryRepository storyRepository;
    private final ParsedRowMapper mapper;
    private final HttpService httpService;

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

    /**
     * @param data - входящая JSON-строка с данными номенклатуры Сварка ПТК
     */
    public ResponseEntity<HttpStatus> parsePtkJsonData(String data) {


        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getPtkJsonData() {
        UriComponents builder = UriComponentsBuilder.newInstance()
                .scheme("https").host("ptk-svarka.ru").path("/files/items.json").build();
        log.info("Запрос из ПТК (ptk-svarka.ru) номенклатурных данных...");
        return httpService.getRestTemplate().exchange(builder.toUriString(), HttpMethod.GET, null, String.class);
    }

    public ResponseEntity<HttpStatus> reloadPtkData() throws JsonProcessingException {
        List<BaseWebModelDTO> newDataArr = new JsonMapper().readValue(getPtkJsonData().getBody(), new TypeReference<>() {
        });

        return ResponseEntity.ok().build();
    }
}
