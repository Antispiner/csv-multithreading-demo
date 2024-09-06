package com.verygoodbank.tes.web.controller;


import com.verygoodbank.tes.service.TradeEnrichmentService;
import com.verygoodbank.tes.service.TradeEnrichmentV2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/v1")
public class TradeEnrichmentController {

    private final TradeEnrichmentService tradeEnrichmentService;
    private final TradeEnrichmentV2Service tradeEnrichmentV2Service;

    @Autowired
    public TradeEnrichmentController(
            TradeEnrichmentService tradeEnrichmentService,
            TradeEnrichmentV2Service tradeEnrichmentV2Service) {
        this.tradeEnrichmentService = tradeEnrichmentService;
        this.tradeEnrichmentV2Service = tradeEnrichmentV2Service;
    }

    @PostMapping(value = "/enrich")
    public String enrichTradeData(@RequestBody String tradeCsvPath) throws IOException, InterruptedException {
        //TODO validation,exceptions etc... Implementation with path should be changed. This Api just for test data by rest, but the main exc through maven build
        var tradeResource = new ClassPathResource(tradeCsvPath);
        return tradeEnrichmentV2Service.enrichTrades(tradeResource.getInputStream());
    }

}


