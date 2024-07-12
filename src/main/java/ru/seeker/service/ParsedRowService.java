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
import ru.seeker.dto.ItemDTO;
import ru.seeker.dto.SheetDTO;
import ru.seeker.dto.WebAdaptDTO;
import ru.seeker.entity.Item;
import ru.seeker.entity.Sheet;
import ru.seeker.mapper.ItemMapper;
import ru.seeker.mapper.SheetMapper;
import ru.seeker.repository.FilesStoryRepository;
import ru.seeker.repository.ItemRepository;
import ru.seeker.repository.SheetRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ParsedRowService {
    private final SheetRepository sheetRepository;
    private final ItemRepository itemRepository;
    private final FilesStoryRepository storyRepository;
    private final SheetMapper sheetMapper;
    private final ItemMapper itemMapper;
    private final HttpService httpService;

    @Transactional(readOnly = true)
    public Page<WebAdaptDTO> findAllByText(String text, int count, int page) {
        log.info("Поиск в базе по строке '{}'...", text.toLowerCase());
        Page<Item> found = itemRepository.findAllByText(text.toLowerCase(), Pageable.ofSize(count).withPage(page));
        log.info("Найдено в базе совпадений по тексту '{}': {} шт.", text, found.getContent().size());
        return itemMapper.toWebAdaptDto(found);
    }

    public ResponseEntity<HttpStatus> deleteAllDataByDocName(String docName) {
        boolean isExists = sheetRepository.existsByDocName(docName);
        if (!isExists) {
            log.info("Не найден на удаление документ с именем '{}'", docName);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.warn("Удаление данных документа '{}'...", docName);
        sheetRepository.deleteAllByDocNameIgnoreCase(docName);
        storyRepository.deleteByDocName(docName);
        log.warn("Документ '{}' был удалён из базы.", docName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> deleteAllDataBySheetName(String sheetName) {
        boolean isExists = sheetRepository.existsByDocName(sheetName);
        if (!isExists) {
            log.info("Не найдена на удаление страница с именем '{}'", sheetName);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.warn("Удаление данных страницы '{}'...", sheetName);
        sheetRepository.deleteAllBySheetNameIgnoreCase(sheetName);
        log.warn("Страница '{}' была удалёна из базы.", sheetName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void save(SheetDTO toSave) {
        log.info("Сохранение в БД sheet '{}'...", toSave.getSheetName());

        Sheet sheet = sheetMapper.toEntity(toSave);
        sheet.getItems().forEach(item -> item.setSheet(sheet));
        sheetRepository.save(sheet);
    }

    public ResponseEntity<String> getPtkJsonData() {
        UriComponents builder = UriComponentsBuilder.newInstance()
                .scheme("https").host("ptk-svarka.ru").path("/files/items.json").build();
        log.info("Запрос из ПТК (ptk-svarka.ru) номенклатурных данных...");
        return httpService.getRestTemplate().exchange(builder.toUriString(), HttpMethod.GET, null, String.class);
    }

    public ResponseEntity<HttpStatus> reloadPtkData() throws JsonProcessingException {
        log.info("Запрос номенклатурных данных из 'ptk-svarka.ru'...");

        Set<ItemDTO> arr = new JsonMapper().readValue(getPtkJsonData().getBody(), new TypeReference<>() {
        });
        log.info("Из 'ptk-svarka.ru' получено единиц товаров {}. Предварительная обработка...", arr.size());
        List<Item> ents = itemMapper.toEntity(arr);

        log.info("Предварительная обработка завершена. Сохраняем...");
        ZonedDateTime now = ZonedDateTime.now();
        Sheet sh = sheetRepository.save(Sheet.builder()
                .docName("ПТК сварка %s.%s.%s".formatted(now.getDayOfMonth(), now.getMonth(), now.getYear())).build());
        ents.forEach(item -> item.setSheet(sh));
        List<Item> saved = itemRepository.saveAll(ents);
        sh.setItems(saved);
        sheetRepository.saveAndFlush(sh);

        log.info("Данные из 'ptk-svarka.ru' сохранены в количестве {} шт.", arr.size());
        return ResponseEntity.ok().build();
    }
}
