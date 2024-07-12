package ru.seeker.service;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.seeker.config.ApplicationProperties;
import ru.seeker.config.Constant;
import ru.seeker.dto.ItemDTO;
import ru.seeker.dto.SheetDTO;
import ru.seeker.entity.FileStory;
import ru.seeker.enums.ExcelTableHeaders;
import ru.seeker.exceptions.GlobalServiceException;
import ru.seeker.exceptions.root.ErrorMessages;
import ru.seeker.repository.FilesStoryRepository;
import ru.seeker.utils.ExceptionUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class ExcelService {
    private static final int MAX_ROWS_TO_SAVE_LIMIT = 10_000;

    private final ApplicationProperties props;
    private final ParsedRowService parsedRowService;
    private final FilesStoryRepository filesStoryRepository;

    public ResponseEntity<HttpStatus> parseExcel(MultipartFile file) throws IOException, BiffException {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new GlobalServiceException(ErrorMessages.WRONG_DOCUMENT_TYPE, filename);
        }

        if (filesStoryRepository.findByDocNameAndDocSize(filename, file.getSize()).isPresent()) {
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

        log.info("Таблица {} была успешно распарсена и сохранена в БД.", file.getOriginalFilename());
        return ResponseEntity.ok().build();
    }

    // use XSSFWorkbook, XSSFSheet, XSSFRow, XSSFCell
    private void parseTable(MultipartFile file) throws IOException {
        // Открываем таблицу на чтение:
        try (
                InputStream is = new BufferedInputStream(file.getInputStream());
                org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook(is)
        ) {
            log.info("Загружаемая таблица {} имеет {} страниц.", file.getOriginalFilename(), workbook.getNumberOfSheets());

            int rowsCount = 0;
            for (org.apache.poi.ss.usermodel.Sheet sheet : workbook) {
                log.info("Читаем страницу '{}'...", sheet.getSheetName());

                // заголовок:
                Map<ExcelTableHeaders, Integer> headerMap = new HashMap<>();
                int column = 0;
                for (Cell cell : sheet.getRow(0)) {
                    if (!cell.getCellType().equals(CellType.STRING)) {
                        continue;
                    }
                    boolean isHeaderExists = Arrays.stream(ExcelTableHeaders.values())
                            .map(ExcelTableHeaders::getDescription)
                            .anyMatch(d -> d.equals(cell.getStringCellValue()
                                    .replace(".", "")
                                    .toLowerCase().trim()));
                    if (!isHeaderExists) {
                        log.info("Пропуск неопознанной колонки '{}' листа '{}' таблицы '{}'...",
                                cell.getStringCellValue(), sheet.getSheetName(), file.getOriginalFilename());
                        column++;
                        continue;
                    }

                    log.info("Найдена колонка '{}' под индексом {}", cell.getStringCellValue(), column);
                    headerMap.put(ExcelTableHeaders.valueOf(cell.getStringCellValue().trim().toLowerCase()), column);
                    column++;
                }


                SheetDTO sheetDto = SheetDTO.builder()
                        .docName(file.getOriginalFilename())
                        .sheetName(sheet.getSheetName().trim())
                        .build();
                Iterator<org.apache.poi.ss.usermodel.Row> rowterator = sheet.rowIterator();
                rowterator.next(); // пропуск заголовка.
                while (rowterator.hasNext()) {
                    org.apache.poi.ss.usermodel.Row nextRow = rowterator.next();
                    ItemDTO itemDto = ItemDTO.builder().build();

                    // артикул
                    if (headerMap.containsKey(ExcelTableHeaders.артикул)) {
                        ExcelTableHeaders head = ExcelTableHeaders.артикул;
                        try {
                            Cell cell = nextRow.getCell(headerMap.get(head), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                itemDto.setSku(String.valueOf(cell.getNumericCellValue()));
                            } else if (cell.getCellType().equals(CellType.STRING)) {
                                itemDto.setSku(cell.getStringCellValue().trim());
                            } else if (!cell.getCellType().equals(CellType.BLANK)) {
                                log.warn("Неопознанный тип ячейки! {}", cell.getCellType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // модель
                    if (headerMap.containsKey(ExcelTableHeaders.модель)) {
                        ExcelTableHeaders head = ExcelTableHeaders.модель;
                        try {
                            itemDto.setModel(nextRow.getCell(headerMap.get(head),
                                    Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue().trim());
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // номенклатура
                    if (headerMap.containsKey(ExcelTableHeaders.номенклатура)) {
                        ExcelTableHeaders head = ExcelTableHeaders.номенклатура;
                        try {
                            itemDto.setTitle(nextRow.getCell(headerMap.get(head),
                                            Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue()
                                    .replaceAll("\\s{2,}", " ").trim());
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // цена
                    if (headerMap.containsKey(ExcelTableHeaders.розн)) {
                        ExcelTableHeaders head = ExcelTableHeaders.розн;
                        try {
                            Cell cell = nextRow.getCell(headerMap.get(head), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                itemDto.setPrice(cell.getNumericCellValue());
                            } else if (cell.getCellType().equals(CellType.STRING)) {
                                String value = cell.getStringCellValue().trim();
                                if (!value.isBlank()) {
                                    try {
                                        itemDto.setPrice(Double.parseDouble(value));
                                    } catch (NumberFormatException e) {
                                        log.warn("Некорректная ячейка '{}': [{}]. Exception: {}", head,
                                                cell.getCellType(), e.getMessage());
                                    }
                                }
                            } else if (!cell.getCellType().equals(CellType.BLANK)) {
                                log.warn("Неопознанный тип ячейки! {}", cell.getCellType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // цена опт
                    if (headerMap.containsKey(ExcelTableHeaders.опт)) {
                        ExcelTableHeaders head = ExcelTableHeaders.опт;
                        try {
                            Cell cell = nextRow.getCell(headerMap.get(head), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                itemDto.setOpt(cell.getNumericCellValue());
                            } else if (cell.getCellType().equals(CellType.STRING)) {
                                String value = cell.getStringCellValue().trim();
                                if (!value.isBlank()) {
                                    try {
                                        itemDto.setOpt(Double.parseDouble(value));
                                    } catch (NumberFormatException e) {
                                        log.warn("Некорректная ячейка '{}': [{}]. Exception: {}", head,
                                                cell.getCellType(), e.getMessage());
                                    }
                                }
                            } else if (cell.getCellType().equals(CellType.FORMULA)) {
                                try {
                                    itemDto.setOpt(cell.getNumericCellValue());
                                } catch (IllegalStateException e) {
                                    log.warn("Некорректная ячейка '{}': [{}]. Exception: {}", head,
                                            cell.getCellType(), e.getMessage());
                                }
                            } else if (!cell.getCellType().equals(CellType.BLANK)) {
                                log.warn("Неопознанный тип ячейки! {}", cell.getCellType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // остаток (общий)
                    if (headerMap.containsKey(ExcelTableHeaders.остаток)) {
                        ExcelTableHeaders head = ExcelTableHeaders.остаток;
                        try {
                            Cell cell = nextRow.getCell(headerMap.get(head), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                itemDto.setStock((int) cell.getNumericCellValue());
                            } else if (cell.getCellType().equals(CellType.STRING)) {
                                try {
                                    itemDto.setStock(Integer.parseInt(cell.getStringCellValue()
                                            .replaceAll("\\s", "").trim()));
                                } catch (NumberFormatException e) {
                                    log.warn("Некорректная ячейка '{}': [{}]. Exception: {}", head,
                                            cell.getCellType(), e.getMessage());
                                }
                            } else if (!cell.getCellType().equals(CellType.BLANK)) {
                                log.warn("Неопознанный тип ячейки! {}", cell.getCellType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // ссылка
                    if (headerMap.containsKey(ExcelTableHeaders.ссылка)) {
                        ExcelTableHeaders head = ExcelTableHeaders.ссылка;
                        try {
                            Cell cell = nextRow.getCell(headerMap.get(head), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            if (!cell.getStringCellValue().isBlank()) {
                                itemDto.setLink(cell.getHyperlink().getAddress());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // добавляем в мапу:
                    boolean isUseful = itemDto.isUseful();
                    if (isUseful) {
                        sheetDto.addItem(itemDto);
                    }
                }

                if (sheetDto.getItems().isEmpty()) {
                    log.warn("Ничего не распарсено с листа {}?..", sheet.getSheetName());
                } else {
                    rowsCount += sheetDto.getItems().size();
                    save(sheetDto);
                }
            }

            if (rowsCount > 0) {
                log.info("Распарсено строк: {}. Создание записи о документе...", rowsCount);
                filesStoryRepository.saveAndFlush(FileStory.builder()
                        .docName(file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : null)
                        .docSize(file.getSize())
                        .sheetsCount(workbook.getNumberOfSheets())
                        .rowsCount(rowsCount)
                        .build());
            } else {
                log.warn("Ничего не распарсено из документа {}?..", file.getOriginalFilename());
            }
        }
    }

    // use HSSFWorkbook, HSSFSheet, HSSFRow, HSSFCell
    private void parseOldTable(MultipartFile file) throws IOException, BiffException {
        try (InputStream is = new BufferedInputStream(file.getInputStream())) {
            Workbook workbook = Workbook.getWorkbook(is);
            log.info("Загружаемая таблица {} имеет {} страниц.", file.getOriginalFilename(), workbook.getNumberOfSheets());

            int shCount = 0;
            int rowsCount = 0;
            for (Sheet sheet : workbook.getSheets()) {
                log.info("Читаем страницу '{}'...", sheet.getName());

                // заголовок:
                Map<ExcelTableHeaders, Integer> headerMap = new HashMap<>();
                int column = 0;
                for (jxl.Cell cell : sheet.getRow(0)) {
                    String cellText = cell.getContents().replace(".", "").toLowerCase().trim();
                    boolean isHeaderExists = Arrays.stream(ExcelTableHeaders.values())
                            .map(ExcelTableHeaders::getDescription)
                            .anyMatch(d -> d.equals(cellText));
                    if (!isHeaderExists) {
                        log.info("Пропуск неопознанной колонки '{}' листа '{}' таблицы '{}'...",
                                cell.getContents(), sheet.getName(), file.getOriginalFilename());
                        column++;
                        continue;
                    }

                    log.info("Найдена колонка '{}' под индексом {}", cell.getContents(), column);
                    headerMap.put(ExcelTableHeaders.valueOf(cellText), column);
                    column++;
                }

                SheetDTO sheetDto = SheetDTO.builder()
                        .docName(file.getOriginalFilename())
                        .sheetName(sheet.getName().trim()).build();

                int rCount = 0;
                for (int i = 1; i < sheet.getRows(); i++) {
                    ItemDTO itemDto = ItemDTO.builder().build();
                    jxl.Cell[] currentRowCells = sheet.getRow(i);

                    if (currentRowCells.length < headerMap.size()) {
                        continue; // какая-то левая, полу-пустая строка?
                    }

                    // артикул
                    if (headerMap.containsKey(ExcelTableHeaders.артикул)) {
                        ExcelTableHeaders head = ExcelTableHeaders.артикул;
                        try {
                            if (isKnownType(currentRowCells[headerMap.get(head)])) {
                                itemDto.setSku(currentRowCells[headerMap.get(head)].getContents());
                            } else if (!currentRowCells[headerMap.get(head)]
                                    .getType().equals(jxl.CellType.EMPTY)
                                    && !currentRowCells[headerMap.get(head)].isHidden()
                            ) {
                                log.warn("Неопознанный тип ячейки! {}",
                                        currentRowCells[headerMap.get(head)].getType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // модель
                    if (headerMap.containsKey(ExcelTableHeaders.модель)) {
                        ExcelTableHeaders head = ExcelTableHeaders.модель;
                        try {
                            if (isKnownType(currentRowCells[headerMap.get(head)])) {
                                itemDto.setModel(currentRowCells[headerMap.get(head)].getContents());
                            } else if (!currentRowCells[headerMap.get(head)]
                                    .getType().equals(jxl.CellType.EMPTY)
                                    && !currentRowCells[headerMap.get(head)].isHidden()
                            ) {
                                log.warn("Неопознанный тип ячейки! {}",
                                        currentRowCells[headerMap.get(head)].getType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // номенклатура
                    if (headerMap.containsKey(ExcelTableHeaders.номенклатура)) {
                        ExcelTableHeaders head = ExcelTableHeaders.номенклатура;
                        try {
                            if (isKnownType(currentRowCells[headerMap.get(head)])) {
                                itemDto.setTitle(currentRowCells[headerMap.get(head)].getContents()
                                        .replaceAll("\\s{2,}", " ").trim());
                            } else if (!currentRowCells[headerMap.get(head)]
                                    .getType().equals(jxl.CellType.EMPTY)
                                    && !currentRowCells[headerMap.get(head)].isHidden()
                            ) {
                                log.warn("Неопознанный тип ячейки! {}",
                                        currentRowCells[headerMap.get(head)].getType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // цена
                    if (headerMap.containsKey(ExcelTableHeaders.розн)) {
                        ExcelTableHeaders head = ExcelTableHeaders.розн;
                        try {
                            if (isKnownType(currentRowCells[headerMap.get(head)])
                                    || currentRowCells[headerMap.get(head)]
                                    .getType().equals(jxl.CellType.NUMBER_FORMULA)
                            ) {
                                itemDto.setPrice(Double.parseDouble(
                                        currentRowCells[headerMap.get(head)].getContents()
                                                .replaceAll(",", ".")
                                                .replaceAll("[ \\s]", "").trim()));
                            } else if (!currentRowCells[headerMap.get(head)]
                                    .getType().equals(jxl.CellType.EMPTY)
                                    && !currentRowCells[headerMap.get(head)].isHidden()
                            ) {
                                log.warn("Неопознанный тип ячейки! {}",
                                        currentRowCells[headerMap.get(head)].getType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // цена опт
                    if (headerMap.containsKey(ExcelTableHeaders.опт)) {
                        ExcelTableHeaders head = ExcelTableHeaders.опт;
                        try {
                            if (isKnownType(currentRowCells[headerMap.get(head)])
                                    || currentRowCells[headerMap.get(head)].getType().equals(jxl.CellType.NUMBER_FORMULA)
                            ) {
                                itemDto.setOpt(Double.parseDouble(
                                        currentRowCells[headerMap.get(head)].getContents()
                                                .replaceAll(",", ".")
                                                .replaceAll("[ \\s]", "").trim()));
                            } else if (!currentRowCells[headerMap.get(head)]
                                    .getType().equals(jxl.CellType.EMPTY)
                                    && !currentRowCells[headerMap.get(head)].isHidden()
                            ) {
                                log.warn("Неопознанный тип ячейки! {}",
                                        currentRowCells[headerMap.get(head)].getType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // остаток (общий)
                    if (headerMap.containsKey(ExcelTableHeaders.остаток)) {
                        ExcelTableHeaders head = ExcelTableHeaders.остаток;
                        try {
                            if (isKnownType(currentRowCells[headerMap.get(head)])) {
                                try {
                                    itemDto.setStock(Integer.parseInt(
                                            currentRowCells[headerMap.get(head)].getContents()
                                                    .replace(",", ".")
                                                    .split("\\.")[0]
                                                    .replaceAll("[ \\s]", "").trim()));
                                } catch (NumberFormatException e) {
                                    log.warn("Некорректная ячейка '{}': [{}] ({}). Exception: {}", head,
                                            currentRowCells[headerMap.get(head)].getType(),
                                            currentRowCells[headerMap.get(head)].getContents(), e.getMessage());
                                }
                            } else if (!currentRowCells[headerMap.get(head)]
                                    .getType().equals(jxl.CellType.EMPTY)
                                    && !currentRowCells[headerMap.get(head)].isHidden()
                            ) {
                                log.warn("Неопознанный тип ячейки! {}",
                                        currentRowCells[headerMap.get(head)].getType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // ссылка
                    if (headerMap.containsKey(ExcelTableHeaders.ссылка)) {
                        ExcelTableHeaders head = ExcelTableHeaders.ссылка;
                        try {
                            if (isKnownType(currentRowCells[headerMap.get(head)])) {
                                itemDto.setLink(currentRowCells[headerMap.get(head)].getContents());
                            } else if (!currentRowCells[headerMap.get(head)]
                                    .getType().equals(jxl.CellType.EMPTY)
                                    && !currentRowCells[headerMap.get(head)].isHidden()
                            ) {
                                log.warn("Неопознанный тип ячейки! {}",
                                        currentRowCells[headerMap.get(head)].getType());
                            }
                        } catch (Exception e) {
                            log.error("Fix it: {}", ExceptionUtils.getFullExceptionMessage(e));
                        }
                    }

                    // добавляем в мапу:
                    boolean isUseful = itemDto.isUseful();
                    if (isUseful) {
                        sheetDto.addItem(itemDto);
                        rCount++;
                    }
                }
                log.info("Распарсено строк: {}", rCount);

                if (rCount > 0) {
                    // сохранение строк страницы в БД (сли в ней были строки):
                    save(sheetDto);
                    rowsCount += rCount;
                }
                shCount++;
            }
            workbook.close();

            filesStoryRepository.saveAndFlush(FileStory.builder()
                    .docName(file.getOriginalFilename() != null ? file.getOriginalFilename().trim() : null)
                    .docSize(file.getSize())
                    .sheetsCount(shCount)
                    .rowsCount(rowsCount)
                    .build());
        }

    }

    private boolean isKnownType(jxl.Cell cell) {
        return !cell.isHidden() && (cell.getType().equals(jxl.CellType.LABEL) || cell.getType().equals(jxl.CellType.NUMBER));
    }

    private void save(SheetDTO sheet) {
        parsedRowService.save(sheet);
    }
}
