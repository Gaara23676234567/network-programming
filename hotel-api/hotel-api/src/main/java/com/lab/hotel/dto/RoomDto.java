package com.lab.hotel.dto;

import com.lab.hotel.model.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private String roomNumber;
    private Room.RoomType type;
    private BigDecimal pricePerNight;
    private Integer capacity;

    public static RoomDto from(Room room) {
        return new RoomDto(
                room.getId(),
                room.getRoomNumber(),
                room.getType(),
                room.getPricePerNight(),
                room.getCapacity()
        );
    }
}
