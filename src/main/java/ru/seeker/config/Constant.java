package ru.seeker.config;

import lombok.Getter;

@Getter
public final class Constant {
    public static final byte NEXT_STATUS_SWITCH_MINUTES = 30;
    public static final short SCHEDULER_POOL_SIZE = 5;

    public static final String OLD_EXCEL_REPORT_EXTENSION = "xls";
    public static final String EXCEL_REPORT_EXTENSION = "xlsx";
    public static final String PDF_REPORT_EXTENSION = "pdf";

    private Constant() {
    }
}
