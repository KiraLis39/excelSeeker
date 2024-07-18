package ru.seeker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.seeker.dto.WebAdaptDTO;
import ru.seeker.exceptions.GlobalServiceException;
import ru.seeker.exceptions.root.ErrorMessages;
import ru.seeker.service.AuthService;
import ru.seeker.service.ParseService;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Работа с данными", description = "Работа с данными")
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class SearchController {
    private final ParseService rowService;
    private final AuthService authService;

    @Operation(summary = "Поиск данных по запросу", description = "Поиск данных по запросу из поисковой строки сайта")
    @ApiResponse(responseCode = "200", description = "Поиск успешно выполнен")
    @ApiResponse(responseCode = "400", description = "Описание ошибки согласно документации")
    @ApiResponse(responseCode = "500", description = "Другая/неожиданная ошибка сервера")
    @GetMapping(path = "/search") //, consumes = {MediaType.APPLICATION_JSON_VALUE}
    public Page<WebAdaptDTO> search(
            @RequestParam("word") String word,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count,
            HttpServletRequest request
    ) {
        if (authService.isAuthUser(request)) {
            return rowService.findAllByText(word, count, page);
        }
        throw new GlobalServiceException(ErrorMessages.SESSION_TIMEOUT);
    }
}
