package com.investai.fullstackproject_ai_chatbot.booking.service;

import com.investai.fullstackproject_ai_chatbot.booking.domain.BookingQuoteRequest;
import com.investai.fullstackproject_ai_chatbot.booking.domain.BookingQuoteResponse;

public interface BookingService {

    BookingQuoteResponse buildQuote(BookingQuoteRequest request);
}
