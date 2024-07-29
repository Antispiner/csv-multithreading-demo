package com.verygoodbank.tes.model;

import java.math.BigDecimal;

public record Trade(String date, int productId, String currency, BigDecimal price) {
    public static final Trade POISON_PILL = new Trade("", -1, "", BigDecimal.ZERO);

    public static Trade fromCsvLine(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length != 4) {
            return null;
        }

        try {
            String date = parts[0].trim();
            int productId = Integer.parseInt(parts[1].trim());
            String currency = parts[2].trim();
            BigDecimal price = new BigDecimal(parts[3].trim());

            return new Trade(date, productId, currency, price);
        } catch (NumberFormatException e) {
            // TODO no more time for logging or something better
            return null;
        }
    }
}
