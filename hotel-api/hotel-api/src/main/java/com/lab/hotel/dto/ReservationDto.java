package com.lab.hotel.dto;

import com.lab.hotel.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    private Long id;
    private Long roomId;
    private String roomNumber;
    private Long hotelId;
    private String hotelName;
    private String guestName;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private Reservation.ReservationStatus status;
    private LocalDateTime createdAt;

    public static ReservationDto from(Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                reservation.getRoom().getId(),
                reservation.getRoom().getRoomNumber(),
                reservation.getRoom().getHotel().getId(),
                reservation.getRoom().getHotel().getName(),
                reservation.getGuestName(),
                reservation.getGuestEmail(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getTotalPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
