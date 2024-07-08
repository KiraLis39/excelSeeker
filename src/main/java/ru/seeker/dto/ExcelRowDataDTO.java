package ru.seeker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Строка данных из таблицы")
public class ExcelRowDataDTO {
    @JsonProperty("docName")
    @Schema(description = "Имя документа-источника данных")
    private String docName;

    @JsonProperty("article")
    @Schema(nullable = true)
    private String article;

    @JsonProperty("model")
    @Schema(nullable = true)
    private String model;

    @JsonProperty("company")
    @Schema(nullable = true)
    private String company;

    @JsonProperty("category")
    @Schema(nullable = true)
    private String category;

    @JsonProperty("price")
    @Schema(nullable = true)
    private String price;


    @CreatedDate
    @JsonProperty(value = "parsedDate")
    private ZonedDateTime parsedDate;
}
