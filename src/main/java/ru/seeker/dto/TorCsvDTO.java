package ru.seeker.dto;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TorCsvDTO {
    // Артикул:
    @CsvBindByPosition(position = 0)
    private String article;

    // Название:
    @CsvBindByPosition(position = 1)
    private String nomenclature;

    // Цена:
    @CsvBindByPosition(position = 2)
    private double rozn;

    // Цена опт:
    @CsvBindByPosition(position = 3)
    private double opt;

    // Общий остаток:
    @CsvBindByPosition(position = 24)
    private int stock;
}
