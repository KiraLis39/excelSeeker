package ru.seeker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
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
import ru.seeker.dto.TorCsvDTO;
import ru.seeker.dto.WebAdaptDTO;
import ru.seeker.entity.Item;
import ru.seeker.entity.Sheet;
import ru.seeker.exceptions.GlobalServiceException;
import ru.seeker.exceptions.root.ErrorMessages;
import ru.seeker.mapper.ItemMapper;
import ru.seeker.repository.ItemRepository;
import ru.seeker.repository.SheetRepository;

import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ParseService {
    private final String TOR_REMOVE_OLD_DATA_PREFIX = "ТОР ";
    private final String PTK_REMOVE_OLD_DATA_PREFIX = "ПТК сварка ";
    private final SheetRepository sheetRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final HttpService httpService;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public Page<WebAdaptDTO> findAllByText(String text, int count, int page) {
        log.info("Поиск в базе по строке '{}'...", text.toLowerCase());
        Page<Item> found = itemRepository.findAllByText(text
                        .toLowerCase().trim()
                        .replaceAll("\\s", "%"),
                Pageable.ofSize(count).withPage(page));
        log.info("Найдено в базе совпадений по тексту '{}': {} шт.", text, found.getContent().size());
        return itemMapper.toWebAdaptDto(found);
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
        log.info("Из 'ptk-svarka.ru' получено единиц товаров {}.", arr.size());

        if (arr.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.JSON_PARSE_ERROR,
                    "Ничего не распарсено из " + PTK_REMOVE_OLD_DATA_PREFIX + "?");
        }

        log.info("Предварительная обработка...");
        List<Item> ents = itemMapper.toEntity(arr);

        log.info("Удаление старого блока '{}'...", PTK_REMOVE_OLD_DATA_PREFIX);
        storageService.deleteAllDataBySheetName(PTK_REMOVE_OLD_DATA_PREFIX);

        log.info("Предварительная обработка завершена. Сохраняем блок '{}", PTK_REMOVE_OLD_DATA_PREFIX);
        ZonedDateTime now = ZonedDateTime.now();
        Sheet sh = sheetRepository.save(Sheet.builder()
                .docName("ПТК сварка %s.%s.%s".formatted(now.getDayOfMonth(), now.getMonth(), now.getYear()))
                .sheetName("ПТК сварка %s.%s.%s".formatted(now.getDayOfMonth(), now.getMonth(), now.getYear()))
                .build());
        ents.forEach(item -> {
            item.setSheet(sh);
            item.setDescription(cleanDescription(item.getDescription()));
        });
        List<Item> saved = itemRepository.saveAll(ents);
        sh.setItems(saved);
        sheetRepository.saveAndFlush(sh);

        log.info("Данные из 'ptk-svarka.ru' сохранены в количестве {} шт.", arr.size());
        return ResponseEntity.ok().build();
    }

    public String cleanDescription(String description) {
        description = description == null || description.isBlank() ? null : description
                .replace("&nbsp;", "")
                .replace("Shorts видео", "")
                .replaceAll("<[^>]*>", "") // все теги.
                .replaceAll(" ", " ")
                .replaceAll("\\s{2,}", " ") //  и более пробелов подряд.
                .trim();
        return description == null || description.isBlank() ? null : description;
    }

    public ResponseEntity<HttpStatus> reloadTorData() {
        UriComponents builder = UriComponentsBuilder.newInstance()
                .scheme("https").host("eme54.ru").path("/partners-im/stoke.csv").build();
        log.info("Запрос из Тор (eme54.ru) номенклатурных данных...");
        List<TorCsvDTO> beans = httpService.getRestTemplate()
                .execute(builder.toUriString(), HttpMethod.GET, null, clientHttpResponse -> {
                    try (InputStreamReader reader = new InputStreamReader(clientHttpResponse.getBody())) {
                        CsvToBean<TorCsvDTO> csvToBean = new CsvToBeanBuilder<TorCsvDTO>(reader)
                                .withType(TorCsvDTO.class)
                                .withSeparator(';')
                                .withIgnoreLeadingWhiteSpace(true)
                                .withSkipLines(1)
                                .build();
                        return csvToBean.stream().collect(Collectors.toList());
                    }
                });
        log.info("Из 'eme54.ru' получено единиц товаров {}.", beans == null || beans.isEmpty() ? 0 : beans.size());

        if (beans == null || beans.isEmpty()) {
            throw new GlobalServiceException(ErrorMessages.CSV_PARSE_ERROR,
                    "Ничего не распарсено из " + TOR_REMOVE_OLD_DATA_PREFIX + "?");
        }

        log.info("Предварительная обработка...");
        List<Item> ents = itemMapper.toEntity(beans);

        log.info("Удаление старого блока '{}'...", TOR_REMOVE_OLD_DATA_PREFIX);
        storageService.deleteAllDataBySheetName(TOR_REMOVE_OLD_DATA_PREFIX);

        ZonedDateTime now = ZonedDateTime.now();
        Sheet sh = sheetRepository.save(Sheet.builder()
                .docName("ТОР %s.%s.%s".formatted(now.getDayOfMonth(), now.getMonth(), now.getYear()))
                .sheetName("ТОР %s.%s.%s".formatted(now.getDayOfMonth(), now.getMonth(), now.getYear()))
                .build());
        ents.forEach(item -> {
            item.setSheet(sh);
            item.setDescription(cleanDescription(item.getDescription()));
//            if (item.getTitle().contains("29142") || item.getSku().equals("1025048")) {
//                log.debug("Цена Розн/Опт из источника: {} руб. / {} руб.\t({} арт.: {})",
//                        item.getPrice(), item.getOpt(), item.getTitle(), item.getSku());
//            }
            item.setOpt(Math.round(item.getOpt() + item.getOpt() * 0.05));
//            if (item.getTitle().contains("29142") || item.getSku().equals("1025048")) {
//                log.debug("Цена Розн/Опт в итоге: {} руб. / {} руб.\t\t\t({} арт.: {})\n",
//                        item.getPrice(), item.getOpt(), item.getTitle(), item.getSku());
//            }
        });
        List<Item> saved = itemRepository.saveAll(ents);

        log.info("Предварительная обработка завершена. Сохраняем блок '{}'...", TOR_REMOVE_OLD_DATA_PREFIX);
        sh.setItems(saved);
        sheetRepository.saveAndFlush(sh);

        log.info("Данные из 'eme54.ru' сохранены в количестве {} шт.", saved.size());
        return ResponseEntity.ok().build();
    }
}
