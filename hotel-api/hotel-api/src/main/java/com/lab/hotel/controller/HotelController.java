package com.lab.hotel.controller;

import com.lab.hotel.dto.BookingRequest;
import com.lab.hotel.dto.ReservationDto;
import com.lab.hotel.dto.RoomDto;
import com.lab.hotel.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class HotelController {

    private final ReservationService reservationService;

    /**
     * GET /hotels/{id}/rooms?date=2024-11-20
     * View available rooms for a hotel on a specific date
     */
    @GetMapping("/hotels/{id}/rooms")
    public ResponseEntity<List<RoomDto>> getAvailableRooms(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<RoomDto> rooms = reservationService.getAvailableRooms(id, date);
        return ResponseEntity.ok(rooms);
    }

    /**
     * POST /hotels/{id}/rooms/{roomId}/book
     * Book a specific room in a hotel
     */
    @PostMapping("/hotels/{id}/rooms/{roomId}/book")
    public ResponseEntity<ReservationDto> bookRoom(
            @PathVariable Long id,
            @PathVariable Long roomId,
            @Valid @RequestBody BookingRequest request) {

        ReservationDto reservation = reservationService.bookRoom(id, roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    /**
     * DELETE /reservations/{id}
     * Cancel a reservation
     */
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}
