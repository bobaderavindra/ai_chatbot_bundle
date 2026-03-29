package com.investai.fullstackproject_ai_chatbot.booking.api;

import com.investai.fullstackproject_ai_chatbot.booking.domain.BookingQuoteRequest;
import com.investai.fullstackproject_ai_chatbot.booking.domain.BookingQuoteResponse;
import com.investai.fullstackproject_ai_chatbot.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/quote")
    public BookingQuoteResponse quote(@Valid @RequestBody BookingQuoteRequest request) {
        return bookingService.buildQuote(request);
    }
}
