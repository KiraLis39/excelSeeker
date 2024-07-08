package ru.seeker.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ExceptionUtils {
    public static String getFullExceptionMessage(Exception e) {
        return e.getCause() == null
                ? e.getMessage() : e.getCause().getCause() == null
                ? e.getCause().getMessage() : e.getCause().getCause().getMessage();
    }
}
