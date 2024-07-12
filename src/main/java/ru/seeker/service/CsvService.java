package ru.seeker.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.seeker.dto.TorCsvDTO;
import ru.seeker.entity.Item;
import ru.seeker.entity.Sheet;
import ru.seeker.mapper.ItemMapper;
import ru.seeker.repository.ItemRepository;
import ru.seeker.repository.SheetRepository;

import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
//@Transactional
@RequiredArgsConstructor
public class CsvService {
    private final HttpService httpService;
    private final ItemMapper itemMapper;
    private final SheetRepository sheetRepository;
    private final ItemRepository itemRepository;

    public ResponseEntity<HttpStatus> loadAndParse() {
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

        log.info("Из 'eme54.ru' получено единиц товаров {}. Предварительная обработка...", beans.size());
        List<Item> ents = itemMapper.toEntity(beans);

        log.info("Предварительная обработка завершена. Сохраняем...");
        ZonedDateTime now = ZonedDateTime.now();
        Sheet sh = sheetRepository.save(Sheet.builder()
                .docName("ТОР %s.%s.%s".formatted(now.getDayOfMonth(), now.getMonth(), now.getYear())).build());
        ents.forEach(item -> item.setSheet(sh));
        List<Item> saved = itemRepository.saveAll(ents);
        sh.setItems(saved);
        sheetRepository.saveAndFlush(sh);

        log.info("Данные из 'eme54.ru' сохранены в количестве {} шт.", saved.size());
        return ResponseEntity.ok().build();
    }
}
