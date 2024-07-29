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
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TradeEnrichmentService {

    private static final Logger logger = LoggerFactory.getLogger(TradeEnrichmentService.class);
    private static final String MISSING_PRODUCT_NAME = "Missing Product Name";
    private static final int QUEUE_CAPACITY = 10000;
    private static final int NUM_CONSUMER_THREADS = 8;

    public String enrichTrades(InputStream tradeCsvStream) throws IOException, InterruptedException {
        //static data
        var productCsvStream = new ClassPathResource("product.csv").getInputStream();
        var productMap = CSVUtils.loadProducts(productCsvStream)
                .stream()
                .collect(Collectors.toConcurrentMap(Product::productId, Product::productName));

        // preparing for response
        //var is not usable here and not readable
        List<String> enrichedDataList = new CopyOnWriteArrayList<>();
        BlockingQueue<Trade> tradeQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(NUM_CONSUMER_THREADS);
        CountDownLatch latch = new CountDownLatch(NUM_CONSUMER_THREADS);

        //Consumer threads
        for (int i = 0; i < NUM_CONSUMER_THREADS; i++) {
            consumerExecutor.submit(() -> {
                try {
                    while (true) {
                        var trade = tradeQueue.poll(1, TimeUnit.SECONDS);
                        //TODO need to work
                        if (trade == null || trade == Trade.POISON_PILL) {
                            break;
                        }
                        processTrade(trade, productMap, enrichedDataList);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Producer thread
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(tradeCsvStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Trade trade = CSVUtils.parseTrade(line);
                if (trade != null && ValidationUtils.isValidDate(trade.date())) {
                    tradeQueue.offer(trade, 1, TimeUnit.SECONDS);
                } else {
                    logger.error("Invalid trade line: {}", line);
                }
            }
        }

        // Add poison pills to signal consumers to exit
        for (int i = 0; i < NUM_CONSUMER_THREADS; i++) {
            tradeQueue.offer(Trade.POISON_PILL, 1, TimeUnit.SECONDS);
        }

        latch.await(); // Wait for consumers to finish
        consumerExecutor.shutdown();

        // need time for better style. Issue with threads validation data, need to work.
        return enrichedDataList.stream()
                .collect(Collectors.joining("\n", "date,product_name,currency,price\n", ""));
    }

    private void processTrade(Trade trade, Map<Integer, String> productMap, List<String> enrichedDataList) {
        var productName = productMap.getOrDefault(trade.productId(), MISSING_PRODUCT_NAME);

        if (MISSING_PRODUCT_NAME.equals(productName)) {
            logger.warn("Missing product name for product ID: {}", trade.productId());
        }
        var enrichedTrade = String.format("%s,%s,%s,%.2f", trade.date(), productName, trade.currency(), trade.price());
        //response result
        enrichedDataList.add(enrichedTrade);
    }
}

