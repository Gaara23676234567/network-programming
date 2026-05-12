-- Hotels
INSERT INTO hotels (id, name, address, star_rating) VALUES (1, 'Grand Palace Hotel', 'вул. Соборна 10, Миколаїв', 5);
INSERT INTO hotels (id, name, address, star_rating) VALUES (2, 'City Inn', 'просп. Центральний 25, Миколаїв', 3);

-- Rooms for Hotel 1
INSERT INTO rooms (id, hotel_id, room_number, type, price_per_night, capacity) VALUES (1, 1, '101', 'STANDARD', 1200.00, 2);
INSERT INTO rooms (id, hotel_id, room_number, type, price_per_night, capacity) VALUES (2, 1, '102', 'STANDARD', 1200.00, 2);
INSERT INTO rooms (id, hotel_id, room_number, type, price_per_night, capacity) VALUES (3, 1, '201', 'DELUXE', 2500.00, 2);
INSERT INTO rooms (id, hotel_id, room_number, type, price_per_night, capacity) VALUES (4, 1, '301', 'SUITE', 5000.00, 4);

-- Rooms for Hotel 2
INSERT INTO rooms (id, hotel_id, room_number, type, price_per_night, capacity) VALUES (5, 2, '101', 'STANDARD', 800.00, 2);
INSERT INTO rooms (id, hotel_id, room_number, type, price_per_night, capacity) VALUES (6, 2, '102', 'STANDARD', 800.00, 2);
INSERT INTO rooms (id, hotel_id, room_number, type, price_per_night, capacity) VALUES (7, 2, '201', 'DELUXE', 1500.00, 3);
