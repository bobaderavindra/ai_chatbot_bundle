package com.investai.fullstackproject_ai_chatbot.pricing.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.investai.fullstackproject_ai_chatbot.pricing.domain.PricePredictionRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class HeuristicPricePredictionServiceTests {

    private final HeuristicPricePredictionService service = new HeuristicPricePredictionService();

    @Test
    void predictsIncreaseForShortWindowWeekendStay() {
        var response = service.predict(new PricePredictionRequest(
                "Bali",
                "family",
                BigDecimal.valueOf(180),
                2,
                true,
                true
        ));

        assertThat(response.predictedPrice()).isGreaterThan(BigDecimal.valueOf(180));
        assertThat(response.trend()).isEqualTo("upward");
    }
}
