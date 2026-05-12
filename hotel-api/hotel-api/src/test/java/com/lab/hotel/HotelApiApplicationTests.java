package com.lab.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.hotel.dto.BookingRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HotelApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /hotels/1/rooms?date=... - should return available rooms")
    void getAvailableRooms_shouldReturnRooms() throws Exception {
        mockMvc.perform(get("/hotels/1/rooms")
                        .param("date", "2025-12-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].roomNumber").exists())
                .andExpect(jsonPath("$[0].pricePerNight").exists());
    }

    @Test
    @DisplayName("GET /hotels/999/rooms?date=... - should return 404 for unknown hotel")
    void getAvailableRooms_hotelNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/hotels/999/rooms")
                        .param("date", "2025-12-20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hotel not found with id: 999"));
    }

    @Test
    @DisplayName("POST /hotels/1/rooms/1/book - should create reservation")
    void bookRoom_shouldCreateReservation() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setGuestName("Іван Петренко");
        request.setGuestEmail("ivan@example.com");
        request.setCheckInDate(LocalDate.of(2025, 12, 20));
        request.setCheckOutDate(LocalDate.of(2025, 12, 25));

        mockMvc.perform(post("/hotels/1/rooms/1/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.guestName").value("Іван Петренко"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalPrice").value(6000.0));
    }

    @Test
    @DisplayName("DELETE /reservations/{id} - should cancel reservation")
    void cancelReservation_shouldReturnNoContent() throws Exception {
        // First, create a reservation
        BookingRequest request = new BookingRequest();
        request.setGuestName("Марія Коваль");
        request.setGuestEmail("maria@example.com");
        request.setCheckInDate(LocalDate.of(2026, 1, 10));
        request.setCheckOutDate(LocalDate.of(2026, 1, 15));

        String response = mockMvc.perform(post("/hotels/1/rooms/2/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long reservationId = objectMapper.readTree(response).get("id").asLong();

        // Then cancel it
        mockMvc.perform(delete("/reservations/" + reservationId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST book - invalid dates should return 400")
    void bookRoom_invalidDates_shouldReturn400() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setGuestName("Test Guest");
        request.setGuestEmail("test@example.com");
        request.setCheckInDate(LocalDate.of(2025, 12, 25));
        request.setCheckOutDate(LocalDate.of(2025, 12, 20)); // before check-in!

        mockMvc.perform(post("/hotels/1/rooms/1/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
