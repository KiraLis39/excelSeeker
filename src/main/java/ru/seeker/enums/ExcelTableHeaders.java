package ru.seeker.enums;

public enum ExcelTableHeaders {
    артикул("артикул"),
    модель("модель"),
    номенклатура("номенклатура"),
    описание("описание"),
    опт("опт"),
    розн("розн"),
    остаток("остаток"),
    ссылка("ссылка");

    private final String description;

    ExcelTableHeaders(String description) {
        this.description = description;
    }

    public Object getDescription() {
        return this.description;
    }
}
