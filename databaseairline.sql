-- =====================================================
-- AIRLINE MANAGEMENT SYSTEM DATABASE
-- =====================================================

CREATE DATABASE airline_db;
USE airline_db;

-- =====================================================
-- USERS TABLE
-- =====================================================

CREATE TABLE users
(
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    mobile VARCHAR(10) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- FLIGHTS TABLE
-- =====================================================

CREATE TABLE flights
(
    flight_id VARCHAR(10) PRIMARY KEY,
    from_city VARCHAR(50) NOT NULL,
    to_city VARCHAR(50) NOT NULL,
    time_slot VARCHAR(20),
    flight_type ENUM('Domestic','International'),
    economy_price DECIMAL(10,2),
    business_price DECIMAL(10,2),
    total_economy_seats INT DEFAULT 50,
    total_business_seats INT DEFAULT 10,
    cancelled BOOLEAN DEFAULT FALSE
);

-- =====================================================
-- HOTELS TABLE
-- =====================================================

CREATE TABLE hotels
(
    hotel_id VARCHAR(10) PRIMARY KEY,
    hotel_name VARCHAR(100),
    city VARCHAR(50),
    price_per_night DECIMAL(10,2)
);

-- =====================================================
-- TAXIS TABLE
-- =====================================================

CREATE TABLE taxis
(
    taxi_id VARCHAR(10) PRIMARY KEY,
    driver_name VARCHAR(100),
    city VARCHAR(50),
    mobile VARCHAR(10),
    per_km_price DECIMAL(10,2)
);

-- =====================================================
-- BANK CARDS TABLE
-- =====================================================

CREATE TABLE bank_cards
(
    card_number VARCHAR(16) PRIMARY KEY,
    holder_name VARCHAR(100),
    cvv VARCHAR(3),
    premium BOOLEAN,
    bank VARCHAR(50)
);

-- =====================================================
-- PAYMENTS TABLE
-- =====================================================

CREATE TABLE payments
(
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_mobile VARCHAR(10),
    user_name VARCHAR(100),
    card_last4 VARCHAR(20),
    bank VARCHAR(50),
    is_premium BOOLEAN,
    original_amount DECIMAL(10,2),
    discount_percent DECIMAL(5,2),
    final_amount DECIMAL(10,2),
    payment_time VARCHAR(50)
);

-- =====================================================
-- TICKETS TABLE
-- =====================================================



CREATE TABLE tickets
(
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_code VARCHAR(30) UNIQUE NOT NULL,

    type VARCHAR(30),

    user_mobile VARCHAR(10),
    user_name VARCHAR(100),

    passenger_name VARCHAR(100),
    passenger_age INT,
    passenger_gender VARCHAR(10),

    details TEXT,
    amount_paid DECIMAL(10,2),
    booking_time VARCHAR(50),

    cancelled BOOLEAN DEFAULT FALSE
);
-- =====================================================
-- WAITING QUEUE TABLE
-- =====================================================

CREATE TABLE waiting_queue
(
    waiting_id INT AUTO_INCREMENT PRIMARY KEY,
    passenger_name VARCHAR(100),
    flight_id VARCHAR(10),
    seat_type VARCHAR(20),
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'WAITING'
);

-- =====================================================
-- SETTINGS TABLE
-- =====================================================

CREATE TABLE settings
(
    id INT PRIMARY KEY,
    tax_rate DECIMAL(5,2)
);
CREATE TABLE booked_seats
(
    seat_id INT AUTO_INCREMENT PRIMARY KEY,

    flight_id VARCHAR(10) NOT NULL,
    travel_date DATE NOT NULL,

    seat_number VARCHAR(10) NOT NULL,
    seat_type ENUM('Business', 'Economy') NOT NULL,
    seat_position ENUM('Window', 'Middle', 'Aisle') NOT NULL,

    passenger_name VARCHAR(100) NOT NULL,
    user_mobile VARCHAR(10),

    ticket_code VARCHAR(30),

    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (flight_id, travel_date, seat_number)
);
INSERT INTO settings
VALUES (1,0.05);
INSERT INTO flights VALUES
('AI101','Ahmedabad','Delhi','09:00','Domestic',4500,9500,50,10,0),
('AI102','Delhi','Ahmedabad','18:00','Domestic',4500,9500,50,10,0),
('AI103','Ahmedabad','Mumbai','13:00','Domestic',3500,8500,50,10,0),
('AI104','Mumbai','Delhi','22:00','Domestic',5000,10000,50,10,0),
('AI105','Ahmedabad','Dubai','04:00','International',28000,65000,50,10,0),
('AI106','Mumbai','London','09:00','International',45000,110000,50,10,0);
INSERT INTO hotels VALUES
('H1','Ahmedabad Hotel 1','Ahmedabad',2500),
('H2','Delhi Hotel 1','Delhi',3200),
('H3','Mumbai Hotel 1','Mumbai',4000),
('H4','Dubai Hotel 1','Dubai',9000),
('H5','London Hotel 1','London',12000);
INSERT INTO taxis VALUES
('T1','Ramesh','Ahmedabad','9876543210',10),
('T2','Suresh','Delhi','9876543211',10),
('T3','Amit','Mumbai','9876543212',10),
('T4','Vijay','Bangalore','9876543213',10);
ALTER TABLE waiting_queue
ADD COLUMN travel_date DATE AFTER flight_id,
ADD COLUMN user_mobile VARCHAR(10) AFTER passenger_name,
ADD COLUMN persons INT DEFAULT 1 AFTER seat_type;
ALTER TABLE tickets
ADD COLUMN seat_number VARCHAR(10) AFTER passenger_gender,
ADD COLUMN seat_position VARCHAR(20) AFTER seat_number;
ALTER TABLE tickets
ADD COLUMN cancellation_percent DECIMAL(5,2) DEFAULT 0,
ADD COLUMN cancellation_charge DECIMAL(10,2) DEFAULT 0,
ADD COLUMN refund_amount DECIMAL(10,2) DEFAULT 0,
ADD COLUMN cancelled_at VARCHAR(50);
CREATE PROCEDURE saveUser(
    IN p_name VARCHAR(100),
    IN p_mobile VARCHAR(10),
    IN p_email VARCHAR(100)
)
BEGIN
    INSERT INTO users(full_name, mobile, email)
    VALUES(p_name, p_mobile, p_email);
END 
CREATE PROCEDURE saveBankCard(
    IN p_cardNumber VARCHAR(16),
    IN p_holderName VARCHAR(100),
    IN p_cvv VARCHAR(3),
    IN p_bank VARCHAR(50),
    IN p_premium BOOLEAN
)
BEGIN
    INSERT INTO bank_cards
    (card_number, holder_name, cvv, bank_name, premium)
    VALUES
    (p_cardNumber, p_holderName, p_cvv, p_bank, p_premium);
END 
CREATE PROCEDURE savePayment(
    IN p_mobile VARCHAR(10),
    IN p_name VARCHAR(100),
    IN p_cardLast4 VARCHAR(20),
    IN p_bank VARCHAR(50),
    IN p_premium BOOLEAN,
    IN p_original DOUBLE,
    IN p_discount DOUBLE,
    IN p_final DOUBLE,
    IN p_time VARCHAR(50)
)
BEGIN
    INSERT INTO payments
    (user_mobile,user_name,card_last4,bank_name,premium,
     original_amount,discount_percent,final_amount,payment_time)
    VALUES
    (p_mobile,p_name,p_cardLast4,p_bank,p_premium,
     p_original,p_discount,p_final,p_time);
END 
CREATE PROCEDURE saveTicket(
    IN p_ticketCode VARCHAR(30),
    IN p_type VARCHAR(30),
    IN p_mobile VARCHAR(10),
    IN p_userName VARCHAR(100),
    IN p_passengerName VARCHAR(100),
    IN p_age INT,
    IN p_gender VARCHAR(20),
    IN p_details TEXT,
    IN p_amount DOUBLE,
    IN p_bookingTime VARCHAR(50),
    IN p_ticketFile VARCHAR(255)
)
BEGIN
    INSERT INTO tickets
    (ticket_code,type,user_mobile,user_name,passenger_name,
     age,gender,details,amount,booking_time,ticket_file)
    VALUES
    (p_ticketCode,p_type,p_mobile,p_userName,p_passengerName,
     p_age,p_gender,p_details,p_amount,p_bookingTime,p_ticketFile);
END
CREATE PROCEDURE cancelTicket(
    IN p_ticketCode VARCHAR(30)
)
BEGIN
    UPDATE tickets
    SET cancelled = TRUE
    WHERE ticket_code = p_ticketCode;
END 
CREATE PROCEDURE deleteBookedSeat(
    IN p_ticketCode VARCHAR(30)
)
BEGIN
    DELETE FROM booked_seats
    WHERE ticket_code = p_ticketCode;
END 
CREATE PROCEDURE cancelFlight(
    IN p_flightId VARCHAR(20)
)
BEGIN
    UPDATE flights
    SET cancelled = TRUE
    WHERE flight_id = p_flightId;
END 
CREATE PROCEDURE updateTax(
    IN p_taxRate DOUBLE
)
BEGIN
    UPDATE tax_settings
    SET tax_rate = p_taxRate;
END 
CREATE PROCEDURE addHotel(
    IN p_id VARCHAR(20),
    IN p_name VARCHAR(100),
    IN p_city VARCHAR(100),
    IN p_price DOUBLE
)
BEGIN
    INSERT INTO hotels
    (hotel_id,hotel_name,city,price_per_night)
    VALUES
    (p_id,p_name,p_city,p_price);
END 