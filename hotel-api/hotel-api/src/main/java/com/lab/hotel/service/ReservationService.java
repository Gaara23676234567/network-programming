package com.lab.hotel.service;

import com.lab.hotel.dto.BookingRequest;
import com.lab.hotel.dto.ReservationDto;
import com.lab.hotel.dto.RoomDto;
import com.lab.hotel.exception.ResourceNotFoundException;
import com.lab.hotel.exception.RoomNotAvailableException;
import com.lab.hotel.model.Reservation;
import com.lab.hotel.model.Room;
import com.lab.hotel.repository.HotelRepository;
import com.lab.hotel.repository.ReservationRepository;
import com.lab.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    // GET /hotels/{id}/rooms?date=2024-11-20
    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRooms(Long hotelId, LocalDate date) {
        // Check hotel exists
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        List<Room> availableRooms = roomRepository.findAvailableRooms(hotelId, date);

        return availableRooms.stream()
                .map(RoomDto::from)
                .collect(Collectors.toList());
    }

    // POST /hotels/{id}/rooms/{roomId}/book
    @Transactional
    public ReservationDto bookRoom(Long hotelId, Long roomId, BookingRequest request) {
        // Validate dates
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        // Find room in this hotel
        Room room = roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + roomId + " in hotel: " + hotelId));

        // Check for conflicting reservations
        boolean isConflict = reservationRepository.existsConflictingReservation(
                roomId, request.getCheckInDate(), request.getCheckOutDate());

        if (isConflict) {
            throw new RoomNotAvailableException(
                    "Room " + room.getRoomNumber() + " is not available for selected dates");
        }

        // Calculate total price
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.setGuestName(request.getGuestName());
        reservation.setGuestEmail(request.getGuestEmail());
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);

        Reservation saved = reservationRepository.save(reservation);
        return ReservationDto.from(saved);
    }

    // DELETE /reservations/{id}
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + reservationId));

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("Reservation is already cancelled");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }
}
