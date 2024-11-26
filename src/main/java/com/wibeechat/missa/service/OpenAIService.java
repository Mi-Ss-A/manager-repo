package com.wibeechat.missa.service;


import com.wibeechat.missa.model.UsageData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${OPENAI_API_KEY}")
    private String openAIApiKey;

    public OpenAIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 특정 날짜의 OpenAI 사용량 데이터를 가져옵니다.
     * @param dateString 요청할 날짜 (예: "2023-10-25")
     * @return UsageData 객체
     * @throws Exception 요청 실패 시 예외 발생
     */
    public UsageData getUsageData(String dateString) throws Exception {
        String url = "https://api.openai.com/v1/usage";

        // 날짜 형식 검증
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate date;
        try {
            date = LocalDate.parse(dateString, formatter);
            logger.info("Parsed date: {}", date);
        } catch (Exception e) {
            logger.error("Invalid date format: {}", dateString, e);
            throw new IllegalArgumentException("Date must be in YYYY-MM-DD format.");
        }

        // URI 빌더 사용
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("date", date.format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAIApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        logger.info("Request URL: {}", builder.toUriString());
        logger.info("Request Headers: {}", headers);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            logger.info("Response status: {}", response.getStatusCode());
            logger.info("Response body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readValue(response.getBody(), UsageData.class);
            } else {
                logger.error("Failed to fetch usage data: {}", response.getStatusCode());
                throw new Exception("Failed to fetch usage data: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Exception while fetching usage data", e);
            throw e;
        }
    }

    /**
     * 현재 날짜의 OpenAI 사용량 데이터를 가져옵니다.
     * @return UsageData 객체
     * @throws Exception 요청 실패 시 예외 발생
     */
    public UsageData getUsageData() throws Exception {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return getUsageData(currentDate);
    }

    public List<UsageData> getWeeklyUsageData() throws Exception {
        LocalDate today = LocalDate.now();
        List<UsageData> weeklyUsageData = new ArrayList<>();

        // 최근 7일 데이터를 가져옵니다.
        for (int i = 0; i < 7; i++) {
            String dateString = today.minusDays(i).format(DateTimeFormatter.ISO_DATE);
            try {
                UsageData usageData = getUsageData(dateString); // 기존 메서드 재사용
                usageData.setDate(dateString); // UsageData에 날짜 추가
                weeklyUsageData.add(usageData);
            } catch (Exception e) {
                logger.error("Failed to fetch usage data for date: {}", dateString, e);
            }
        }
        return weeklyUsageData;
    }
}
