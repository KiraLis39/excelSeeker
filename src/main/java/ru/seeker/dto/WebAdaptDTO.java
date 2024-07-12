package ru.seeker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Модель для веб-морды", hidden = true)
public class WebAdaptDTO {
    @Schema(description = "Категория")
    private String category;

    @Schema(description = "Артикул")
    private String sku;

    @Schema(description = "Модель")
    private String model;

    @Schema(description = "Номенклатура")
    private String title;

    @Schema(description = "Цена розница")
    private double rozn;

    @Schema(description = "Цена опт")
    private double opt;

    @Schema(description = "Наличие всего (после расчетов)")
    private int stock;

    @Schema(description = "Гиперссылка на товар")
    private String link;

    @Schema(description = "Описание")
    private String description;

    private String excerpt;

    @Schema(description = "Имя таблицы-источника")
    private String sheetName;

    @Schema(description = "Имя документа-источника")
    private String docName;

    @Schema(description = "Дата сбора данных")
    private ZonedDateTime parsedDate;
}
