package ru.seeker.exceptions.root;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessages {
    UNIVERSAL_ERROR_TEMPLATE("E0XX", "Ошибка"),
    EXCEL_PARSING_ERROR("E001", "Ошибка парсинга файла Excel"),
    PARTICIPANT_IS_ABSENT_ERROR("E002", "Пользователь не найден"),
    OTHER_SERVICE_CONNECT_ERROR("E003", "Ошибка при взаимодействии с сервисом > "),
    NOT_ENOUGH_DATA("E004", "Не достаточно данных"),
    LENGTH_EXCEEDS_LIMITS("E005", "Длина поля превышает допустимый лимит симолов"),
    FILESYSTEM_ERROR("E006", "Ошибка файловой системы (создания/удаления файла или директории)"),
    WRONG_DOCUMENT_TYPE("E007", "Неподдерживаемый формат файла"),
    WAS_LOADED_ALREADY("E008", "Документ уже загружался ранее"),
    DOCUMENT_ERROR("E009", "Ошибка при обработке документа"),
    JSON_PARSE_ERROR("E010", "Ошибка при разборе полученного json"),
    CSV_PARSE_ERROR("E011", "Ошибка при разборе полученного csv");

    private final String errorCode;
    private final String errorCause;
}
