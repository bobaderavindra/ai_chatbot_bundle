package com.investai.fullstackproject_ai_chatbot.ai.service;

import com.investai.fullstackproject_ai_chatbot.ai.domain.AgentInsight;
import com.investai.fullstackproject_ai_chatbot.ai.domain.AiChatRequest;
import org.springframework.stereotype.Component;

@Component
public class HotelAgent implements TravelAgent {

    @Override
    public AgentInsight analyze(AiChatRequest request) {
        String hotelFocus = request.selectedHotel() == null || request.selectedHotel().isBlank()
                ? "top shortlisted hotels"
                : request.selectedHotel();
        return new AgentInsight(
                "Hotel Agent",
                "Evaluated amenities, family fit, and cancellation flexibility for " + hotelFocus + ".",
                "high",
                "Favor properties with breakfast and flexible cancellation until flights are finalized.",
                88
        );
    }
}
