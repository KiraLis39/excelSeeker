package ru.seeker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jxl.read.biff.BiffException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.seeker.config.ApplicationProperties;
import ru.seeker.entity.FileStory;
import ru.seeker.exceptions.GlobalServiceException;
import ru.seeker.exceptions.root.ErrorMessages;
import ru.seeker.service.ExcelService;
import ru.seeker.service.StorageService;
import ru.seeker.utils.ExceptionUtils;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Работа с документами", description = "Работа с документами")
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class FileController {
    private final ApplicationProperties props;
    private final ExcelService excelService;
    private final StorageService storageService;

    @Operation(summary = "Загрузка документов", description = "Загрузка документа")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Описание ошибки согласно документации")
    @ApiResponse(responseCode = "500", description = "Другая/неожиданная ошибка сервера")
    @PostMapping(path = "/load", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<HttpStatus> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            ZipSecureFile.setMinInflateRatio(props.getPoiZipSecureMinInflateRatio());
            ZipSecureFile.setMaxFileCount(props.getPoiZipSecureMaxFileCount());

            return excelService.parseExcel(file);
        } catch (NoSuchFileException nsfe) {
            throw new GlobalServiceException(ErrorMessages.FILESYSTEM_ERROR, nsfe.getMessage());
        } catch (IOException e) {
            throw new GlobalServiceException(ErrorMessages.EXCEL_PARSING_ERROR, e.getMessage());
        } catch (BiffException be) {
            throw new GlobalServiceException(ErrorMessages.DOCUMENT_ERROR, be.getMessage());
        } catch (Exception e) {
            throw new GlobalServiceException(ErrorMessages.UNIVERSAL_ERROR_TEMPLATE, ExceptionUtils.getFullExceptionMessage(e));
        }
    }

    @Operation(summary = "Удаление файлов", description = "Удаление файлов")
    @ApiResponse(responseCode = "200", description = "Файл успешно удалён",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = String.class))})
    @PostMapping("/delete")
    public ResponseEntity<HttpStatus> deleteFile(@RequestParam String fileName) {
        return storageService.deleteAllDataByDocName(fileName);
    }

//    @Operation(summary = "Получение файла", description = "Получение файла")
//    @ApiResponse(responseCode = "200", description = "Файл успешно обнаружен")
//    @ApiResponse(responseCode = "400", description = "Описание ошибки согласно документации")
//    @ApiResponse(responseCode = "500", description = "Другая/неожиданная ошибка сервера")
//    @GetMapping("/get")
//    public ResponseEntity<byte[]> getFile(@RequestParam String file) {
//        return ResponseEntity.ok(new byte[0]);
//    }

    @Operation(summary = "Список загруженных документов", description = "Получить список загруженных ранее документов")
    @ApiResponse(responseCode = "200", description = "Поиск успешно выполнен")
    @ApiResponse(responseCode = "400", description = "Описание ошибки согласно документации")
    @ApiResponse(responseCode = "500", description = "Другая/неожиданная ошибка сервера")
    @GetMapping(path = "/docs") //, consumes = {MediaType.APPLICATION_JSON_VALUE}
    public Page<FileStory> docList(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count
    ) {
        return storageService.findAllDocuments(count, page);
    }
}
