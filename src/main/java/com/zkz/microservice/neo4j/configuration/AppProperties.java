package com.zkz.microservice.neo4j.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;

/**
 * Created by Jason Zhuang on 17/11/21.
 */
@Configuration
@RequiredArgsConstructor
@Data
public class AppProperties {
    // business parts
    @Value("${customized.application.name}")
    private String customizedApplicationName;

    @Value("${customized.application.description}")
    private String customizedApplicationDescription;

    @Value("${customized.ocr-apis.baseURL}")
    private String ocrApisBaseUrl;

    @Value("${customized.ocr-apis.getChapters}")
    private String ocrApisGetChapters;

    @Value("${customized.ocr-apis.getOutcomesByChapterId}")
    private String ocrApisGetOutcomesByChapterId;

    @Value("${customized.ocr-apis.getOutcomesByChapterTitle}")
    private String ocrApisGetOutcomesByChapterTitle;

    @Value("${customized.ocr-apis.getUnits}")
    private String ocrApisGetUnits;

    @Value("${customized.ocr-apis.getUnitsByChapterId}")
    private String ocrApisGetUnitsByChapterId;

    @Value("${customized.ocr-apis.getTopics}")
    private String ocrApisGetTopics;

    @Value("${customized.ocr-apis.getTopicsByUnitId}")
    private String ocrApisGetTopicsByUnitId;

    @Value("${customized.ocr-apis.getSubTopicsByTopicId}")
    private String ocrApisGetSubTopicsByTopicId;

    @Value("${customized.ocr-apis.getDictionaryByWord}")
    private String ocrApisGetDictionaryByWord;

    // system parts
    @Value("${customized.gcp.key}")
    private String gcpKey;

    // spring parts
    @Value("${spring.mvc.format.date-time}")
    private String formatDateTime;


}
