package com.verygoodbank.tes.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ValidationUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    //i can better
    public static boolean isValidDate(String date) {
        try {
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
