package ru.seeker.service;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.seeker.config.Constant;
import ru.seeker.dto.ParsedRowDTO;
import ru.seeker.entity.FileStory;
import ru.seeker.exceptions.GlobalServiceException;
import ru.seeker.exceptions.root.ErrorMessages;
import ru.seeker.repository.FilesStoryRepository;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class ExcelService {
    private static final String WORD_DELIMITER = "=!";
    private static final String EMPTY_CELL_MOCK = " - ";
    private final HttpService httpService;
    private final ParsedRowService parsedRowService;
    private final FilesStoryRepository filesStoryRepository;

    public ResponseEntity<HttpStatus> parseExcel(MultipartFile file) throws IOException, BiffException {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DOCUMENT_TYPE, filename);
        }

        Optional<FileStory> found = filesStoryRepository.findByDocNameAndDocSize(filename, file.getSize());
        if (found.isPresent()) {
            throw new GlobalServiceException(ErrorMessages.WAS_LOADED_ALREADY);
        }

        log.info("Загрузка файла '{}' ({} байт)...", filename, file.getSize());
        if (filename.endsWith(Constant.EXCEL_REPORT_EXTENSION)) {
            parseTable(file);
        } else if (filename.endsWith(Constant.OLD_EXCEL_REPORT_EXTENSION)) {
            parseOldTable(file);
        } else {
            throw new GlobalServiceException(ErrorMessages.WRONG_DOCUMENT_TYPE, file.getOriginalFilename());
        }

        return ResponseEntity.ok().build();
    }

    // use XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell
    private void parseTable(MultipartFile file) throws IOException {
        // Открываем таблицу на чтение:
        try (
                InputStream is = new BufferedInputStream(file.getInputStream());
                org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook(is)
        ) {
            int pages = workbook.getNumberOfSheets();
            log.info("Загружаемая таблица {} имеет {} страниц.", file.getOriginalFilename(), pages);

            int rowsCount = 0;
            for (org.apache.poi.ss.usermodel.Sheet sheet : workbook) {
                log.info("Читаем страницу {}...", sheet.getSheetName());

                Map<Integer, ParsedRowDTO> rows = new HashMap<>();
                int i = 0;
                for (org.apache.poi.ss.usermodel.Row row : sheet) {
                    rows.put(i, buildNewRowDto(file, sheet));

                    StringBuilder sb = new StringBuilder();
                    for (org.apache.poi.ss.usermodel.Cell cell : row) {
                        String cellData = switch (cell.getCellType()) {
                            case STRING -> cell.getRichStringCellValue().getString();
                            case NUMERIC -> String.valueOf(DateUtil.isCellDateFormatted(cell)
                                    ? cell.getDateCellValue() : cell.getNumericCellValue());
                            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                            case FORMULA -> String.valueOf(
                                    switch (cell.getCachedFormulaResultType()) {
                                        case NUMERIC -> cell.getNumericCellValue();
                                        case STRING -> cell.getRichStringCellValue().getString();
                                        default -> cell.getCellFormula();
                                    });
                            case _NONE, BLANK -> EMPTY_CELL_MOCK;
                            case ERROR -> String.valueOf(cell.getErrorCellValue());
                        };

                        if (cellData != null && !cellData.isBlank()) {
                            sb.append(cleanValue(cellData));
                        } else {
                            sb.append(EMPTY_CELL_MOCK);
                        }
                        sb.append(WORD_DELIMITER);
                    }

                    if (hasUsefulData(sb)) {
                        // добавляем в мапу очередную строку, заполненную ячейками:
                        rows.get(i).setRowData(removeTails(sb.toString()));
                        i++;
                    }
                }
                log.info("Распарсено строк: {}", rows.size());

                // сохранение строк страницы в БД:
                rowsCount += rows.size();
                save(rows.values());
            }

            filesStoryRepository.saveAndFlush(FileStory.builder()
                    .docName(file.getOriginalFilename())
                    .docSize(file.getSize())
                    .sheetsCount(pages)
                    .rowsCount(rowsCount)
                    .build());
        }
    }

    private String removeTails(String string) {
        while (string.endsWith(WORD_DELIMITER)) {
            string = string.substring(0, string.length() - WORD_DELIMITER.length());
            if (string.endsWith(EMPTY_CELL_MOCK)) {
                string = string.substring(0, string.length() - EMPTY_CELL_MOCK.length());
            }
        }
        return string.trim();
    }

    // use HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell
    private void parseOldTable(MultipartFile file) throws IOException, BiffException {
        try (InputStream is = new BufferedInputStream(file.getInputStream())) {
            Workbook workbook = Workbook.getWorkbook(is);
            log.info("Загружаемая таблица {} имеет {} страниц.", file.getOriginalFilename(), workbook.getNumberOfSheets());

            int rCount = 0;
            for (Sheet sheet : workbook.getSheets()) {
                log.info("Читаем страницу {}...", sheet.getName());
                int rowsCount = sheet.getRows();
                Map<Integer, ParsedRowDTO> rows = new HashMap<>(rowsCount);
                for (int i = 0; i < rowsCount; i++) {
                    rows.put(i, buildNewRowDtoOld(file, sheet));

                    StringBuilder sb = new StringBuilder();
                    for (Cell cell : sheet.getRow(i)) {
                        if (cell.isHidden()) {
                            continue;
                        }

                        String cellData = cell.getContents();
                        if (cellData != null && !cellData.isBlank()) {
                            sb.append(cleanValue(cellData));
                        } else {
                            sb.append(EMPTY_CELL_MOCK);
                        }
                        sb.append(WORD_DELIMITER);
                    }

                    if (hasUsefulData(sb)) {
                        // добавляем в мапу очередную строку, заполненную ячейками:
                        rows.get(i).setRowData(removeTails(sb.toString()));
                    }
                }
                log.info("Распарсено строк: {}", rows.size());

                // сохранение строк страницы в БД:
                save(rows.values());
                rCount += rows.size();
            }
            workbook.close();

            filesStoryRepository.saveAndFlush(FileStory.builder()
                    .docName(file.getOriginalFilename())
                    .docSize(file.getSize())
                    .sheetsCount(workbook.getSheets().length)
                    .rowsCount(rCount)
                    .build());
        }
    }

    private boolean hasUsefulData(StringBuilder sb) {
        String tmp = sb.toString()
                .replace(EMPTY_CELL_MOCK, "")
                .replace(WORD_DELIMITER, "")
                .trim();
        return !tmp.isBlank() && !tmp.equals(".") && !tmp.equals(",");
    }

    private ParsedRowDTO buildNewRowDto(MultipartFile file, org.apache.poi.ss.usermodel.Sheet sheet) {
        return ParsedRowDTO.builder()
                .docName(file.getOriginalFilename())
                .sheetName(sheet.getSheetName()).build();
    }

    private ParsedRowDTO buildNewRowDtoOld(MultipartFile file, Sheet sheet) {
        return ParsedRowDTO.builder()
                .docName(file.getOriginalFilename())
                .sheetName(sheet.getName()).build();
    }

    private String cleanValue(String cellData) {
        return cellData
                .replaceAll("\\s{2,}", " ")
                .replaceAll("\\s,\\s", ", ")
                .replaceAll("\\s\\.", ".")
                .replace(" ", " ");
    }

    private void save(Collection<ParsedRowDTO> values) {
        values = values.stream()
                .filter(parsedRowDto -> parsedRowDto.getRowData() != null)
                .collect(Collectors.toList());

        parsedRowService.saveRows(values);
    }
}
