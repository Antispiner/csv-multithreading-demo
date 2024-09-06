package com.verygoodbank.tes;

import com.verygoodbank.tes.service.TradeEnrichmentService;
import com.verygoodbank.tes.service.TradeEnrichmentV2Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
class TradeEnrichmentServiceApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(TradeEnrichmentServiceApplicationTests.class);

    @Autowired
    private TradeEnrichmentService tradeEnrichmentService;

    @Autowired
    private TradeEnrichmentV2Service tradeEnrichmentV2Service;

    private FileInputStream tradeCsvStream;
    private FileInputStream productCsvStream;

    @BeforeEach
    public void setUp() throws IOException {
        tradeCsvStream = new FileInputStream("large-trade.csv");
        productCsvStream = new FileInputStream("large-product.csv");
    }

    //TODO this test just for maven exc
    @Test
    public void testEnrichTradesUnderLoad() throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        //tradeEnrichmentService.enrichTrades(tradeCsvStream);
        tradeEnrichmentV2Service.enrichTrades(tradeCsvStream);
        long endTime = System.currentTimeMillis();
        logger.warn("Processed in " + (endTime - startTime) + " ms");
    }
}
