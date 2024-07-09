package ru.seeker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.net.URL;
import java.util.HashSet;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Модель загрузки данных")
@JsonIgnoreProperties(value = {"id", "sku", "data", "passport", "producer"})
public class BaseWebModelDTO {
    @Schema(description = "Гиперссылка на товар")
    private URL link;

    @Schema(description = "Категория")
    private String category;

    @Schema(description = "Номенклатура")
    private String title;

    private String excerpt;

    @Schema(description = "Описание")
    private String description;

    @Schema(description = "Изображения")
    private HashSet<String> images;

    @Schema(description = "Цена")
    private int price;

    @Schema(description = "Наличие в Москве")
    private int stock_msk;

    @Schema(description = "Наличие в Питере")
    private int stock_spb;

    @Schema(description = "Резерв в Москве")
    private int reserve_msk;

    @Schema(description = "Резерв в Питере")
    private int reserve_spb;

    @Schema(description = "Количество в транспортной упаковке")
    private int kty;

    @Schema(description = "Количество в индивидуальной упаковке")
    private int kiy;
}
