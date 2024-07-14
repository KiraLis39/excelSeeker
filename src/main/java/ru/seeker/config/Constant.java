package ru.seeker.config;

import lombok.Getter;

@Getter
public final class Constant {
    public static final short SCHEDULER_POOL_SIZE = 3;
    public static final long RARE_CHECK_HOURS = 6;
    public static final byte VERY_RARE_CHECK_HOURS = 12;
    public static final byte ONCE_PER_DAY_HOURS = 24;

    public static final String OLD_EXCEL_REPORT_EXTENSION = "xls";
    public static final String EXCEL_REPORT_EXTENSION = "xlsx";
    public static final String PDF_REPORT_EXTENSION = "pdf";
    public static final short SESSION_LIVE_MINUTES = 30;

    private Constant() {
    }
}
