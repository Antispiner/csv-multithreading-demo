package com.verygoodbank.tes.util;

import com.verygoodbank.tes.model.Product;
import com.verygoodbank.tes.model.Trade;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {

    public static List<Product> loadProducts(InputStream productCsvStream) {
        var products = new ArrayList<Product>();
        //deprecated, need to work
        try (CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(new InputStreamReader(productCsvStream))) {
            for (CSVRecord record : csvParser) {
                int productId = Integer.parseInt(record.get("product_id"));
                var productName = record.get("product_name");
                products.add(new Product(productId, productName));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse product CSV", e);
        }
        return products;
    }

    public static Trade parseTrade(String line) {
        return Trade.fromCsvLine(line);
    }
}
