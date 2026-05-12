package com.lab.hotel.repository;

import com.lab.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find all rooms for a hotel that are NOT booked on the given date
    @Query("""
            SELECT r FROM Room r
            WHERE r.hotel.id = :hotelId
            AND r.id NOT IN (
                SELECT res.room.id FROM Reservation res
                WHERE res.status = 'CONFIRMED'
                AND res.checkInDate <= :date
                AND res.checkOutDate > :date
            )
            """)
    List<Room> findAvailableRooms(@Param("hotelId") Long hotelId,
                                  @Param("date") LocalDate date);

    Optional<Room> findByIdAndHotelId(Long roomId, Long hotelId);
}
