package ru.seeker.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(description = "Модель загрузки данных")
@JsonIgnoreProperties(value = {"id", "data", "passport", "producer", "kty", "kiy", "images"})
public class ItemDTO {
    @JsonIgnore
    @Builder.Default
    private UUID uuid = UUID.randomUUID();

    @Schema(description = "Категория")
    private String category;

    @Schema(description = "Артикул")
    private String sku;

    @Schema(description = "Модель")
    private String model;

    @Schema(description = "Номенклатура")
    private String title;

    @Schema(description = "Цена розница")
    private double price;

    @Schema(description = "Цена опт")
    private double opt;

    @Schema(description = "Наличие в Москве")
    private int stock_msk;

    @Schema(description = "Наличие в Питере")
    private int stock_spb;

    @Schema(description = "Резерв в Москве")
    private int reserve_msk;

    @Schema(description = "Резерв в Питере")
    private int reserve_spb;

    @Schema(description = "Наличие всего (после расчетов)")
    private int stock;

    @Schema(description = "Гиперссылка на товар")
    private String link;

    @Schema(description = "Описание")
    private String description;

    private String excerpt;

    @JsonIgnore
    private SheetDTO sheet;

    @JsonIgnore
    public boolean isUseful() {
        boolean hasArticle = sku != null && !sku.isBlank();
        boolean hasModel = model != null && !model.isBlank();
        boolean hasExcerpt = excerpt != null && !excerpt.isBlank();
        boolean hasNomenclature = title != null && !title.isBlank();
        boolean hasRozn = price > 0;
        boolean hasOpt = opt > 0;
        boolean hasStock = stock > 0 || stock_spb - reserve_spb > 0 || stock_msk - reserve_msk > 0;
        boolean hasDescription = description != null && !description.isBlank();

        return hasNomenclature && (hasModel || hasArticle || hasOpt) && (hasRozn || hasDescription || hasExcerpt || hasStock);
    }
}
