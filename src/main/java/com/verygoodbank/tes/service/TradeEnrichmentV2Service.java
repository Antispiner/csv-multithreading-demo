package com.verygoodbank.tes.service;

import com.verygoodbank.tes.model.Product;
import com.verygoodbank.tes.model.Trade;
import com.verygoodbank.tes.util.CSVUtils;
import com.verygoodbank.tes.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service()
public class TradeEnrichmentV2Service {

    private static final Logger logger = LoggerFactory.getLogger(TradeEnrichmentV2Service.class);

    private static final String MISSING_PRODUCT_NAME = "Missing Product Name";
    private static final int QUEUE_CAPACITY = 10000;
    private static final int NUM_CONSUMER_THREADS = 8;

    public String enrichTrades(InputStream tradeCsvStream) throws IOException, InterruptedException {

        // static data
        var productCsvStream = new ClassPathResource("product.csv").getInputStream();
        var productMap = CSVUtils.loadProducts(productCsvStream)
                .stream()
                .collect(Collectors.toMap(Product::productId, Product::productName));

        // preparing for response
        var enrichedDataList = Collections.synchronizedList(new ArrayList<String>());

        // Producer thread
        processCsv(tradeCsvStream, productMap, enrichedDataList);

        // need time for better style. Issue with threads validation data, need to work.
        return enrichedDataList.stream()
                .collect(Collectors.joining("\n", "date,product_name,currency,price\n", ""));
    }

    private void processCsv(
            InputStream tradeCsvStream,
            Map<Integer, String> productMap,
            List<String> enrichedDataList) throws IOException {

        var forkJoinPool = new ForkJoinPool(NUM_CONSUMER_THREADS);

        try (var reader = new BufferedReader(new InputStreamReader(tradeCsvStream))) {

            var bufferSize = QUEUE_CAPACITY / NUM_CONSUMER_THREADS;
            var buffer = new ArrayList<String>(bufferSize);

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.add(line);
                if (buffer.size() >= bufferSize) {
                    submitAndWait(forkJoinPool, productMap, buffer, enrichedDataList);
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                submitAndWait(forkJoinPool, productMap, buffer, enrichedDataList);
                buffer.clear();
            }
        }

        forkJoinPool.shutdown();
    }

    private void submitAndWait(
            ForkJoinPool forkJoinPool,
            Map<Integer, String> productMap,
            List<String> rawTrades,
            List<String> resultContainer) {

        CompletableFuture<String>[] asyncTasks = rawTrades
                .stream()
                .map(CSVUtils::parseTrade)
                .filter(Objects::nonNull)
                .filter(trade -> ValidationUtils.isValidDate(trade.date()))
                .filter(trade -> trade != Trade.POISON_PILL)
                .map(trade -> CompletableFuture.supplyAsync(() -> processTrade(trade, productMap), forkJoinPool))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(asyncTasks).join();

        List<String> processedTrades = Arrays
                .stream(asyncTasks)
                .map(CompletableFuture::join)
                .toList();

        resultContainer.addAll(processedTrades);
    }

    private String processTrade(Trade trade, Map<Integer, String> productMap) {

        var productName = productMap.getOrDefault(trade.productId(), MISSING_PRODUCT_NAME);

        if (MISSING_PRODUCT_NAME.equals(productName)) {
            logger.warn("Missing product name for product ID: {}", trade.productId());
        }

        //response result
        return String.format("%s,%s,%s,%.2f", trade.date(), productName, trade.currency(), trade.price());
    }
}

