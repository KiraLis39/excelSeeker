package ru.seeker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ParsedDocumentDTO {

    @JsonIgnore
    @Schema(description = "Имя документа-источника данных")
    private UUID uuid;

    @Schema(description = "Имя документа-источника данных")
    @JsonProperty("doc_name")
    private String docName;

    @Schema(description = "Дата обработки")
    @JsonProperty("parsed_date")
    private ZonedDateTime parsedDate = ZonedDateTime.now();

    @Builder.Default
    @JsonProperty("row_data")
    @Schema(description = "Коллекция данных строки")
    private List<String> rowData = new ArrayList<>();
}
