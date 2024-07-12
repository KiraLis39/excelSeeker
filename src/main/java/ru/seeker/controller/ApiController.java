package ru.seeker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.seeker.exceptions.GlobalServiceException;
import ru.seeker.exceptions.root.ErrorMessages;
import ru.seeker.service.CsvService;
import ru.seeker.service.ParsedRowService;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Парсинг API raw-data", description = "Работа с raw-данными поставщиков")
@RequestMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiController {
    private final ParsedRowService rowService;
    private final CsvService csvService;

    @Operation(summary = "Получить JSON от ПТК", description = "Получить новый файл JSON от ПТК")
    @ApiResponse(responseCode = "200", description = "Данные успешно обработаны")
    @ApiResponse(responseCode = "400", description = "Описание ошибки согласно документации")
    @ApiResponse(responseCode = "500", description = "Другая/неожиданная ошибка сервера")
    @GetMapping(path = "/get_ptk_json") //, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getPtkJson() {
        return rowService.getPtkJsonData();
    }

    @Operation(summary = "Автозагрузка JSON от ПТК", description = "Загрузка данных ПТК в БД")
    @ApiResponse(responseCode = "200", description = "Данные успешно обработаны")
    @ApiResponse(responseCode = "400", description = "Описание ошибки согласно документации")
    @ApiResponse(responseCode = "500", description = "Другая/неожиданная ошибка сервера")
    @GetMapping(path = "/load_ptk_json") //, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HttpStatus> loadPtkJson() {
        try {
            return rowService.reloadPtkData();
        } catch (JsonProcessingException json) {
            throw new GlobalServiceException(ErrorMessages.JSON_PARSE_ERROR, json.getMessage());
        }
    }

    @Operation(summary = "Автозагрузка CSV от ТОР", description = "Автозагрузка CSV от ТОР")
    @ApiResponse(responseCode = "200", description = "Данные успешно обработаны")
    @ApiResponse(responseCode = "400", description = "Описание ошибки согласно документации")
    @ApiResponse(responseCode = "500", description = "Другая/неожиданная ошибка сервера")
    @GetMapping(path = "/load_tor_csv") //, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HttpStatus> loadTorCsv() {
        return csvService.loadAndParse();
    }
}
