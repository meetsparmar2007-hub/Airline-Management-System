package Main;

import ds.Flight;
import ds.Hotel;
import ds.Taxi;
import ds.BankCard;
import ds.Passenger;
import ds.Ticket;
import DBMS.DatabaseHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;

/**
 * MAIN / CORE LOGIC PACKAGE
 * Contains the runnable entry point and all booking / admin workflow logic.
 * Model classes live in the "ds" package; DB access lives in the "dbms" package.
 */
class AirlineManagementSystem {

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd-MM-uuuu").toFormatter().withResolverStyle(ResolverStyle.STRICT);

    /**
     * Safe read wrapper: exits gracefully instead of crashing if input stream ends unexpectedly.
     */
    static String readLine() throws IOException {
        String line = br.readLine();
        if (line == null) {
            System.out.println("\nInput ended. Closing Airline Booking System. Goodbye!");
            System.exit(0);
        }
        return line.trim();
    }


    // ---------- DS: core in-memory collections ----------

    static ArrayList<Flight> flights = new ArrayList<>();                     // ArrayList
    static HashMap<String, List<Flight>> routeMap = new HashMap<>();         // HashMap: "CityA-CityB" -> flights
    static ArrayList<Hotel> hotels = new ArrayList<>();                       // ArrayList
    static ArrayList<Taxi> taxis = new ArrayList<>();                         // ArrayList
    static ArrayList<Ticket> tickets = new ArrayList<>();                     // ArrayList
    static Hashtable<String, BankCard> validCards = new Hashtable<>();       // Hashtable: known demo cards (premium/bank discount lookup only)
    static Stack<String> activityLog = new Stack<>();   // DS: Stack (LIFO)
    static HashSet<String> cancelledFlightIds = new HashSet<>();
    static LinkedList<String> waitingQueue = new LinkedList<>();
    static double taxRate = 0.05; // 5% default tax, admin can change
    static final String[] DOMESTIC_CITIES = {"Ahmedabad", "Delhi", "Mumbai", "Bangalore"};
    static final String[] INTERNATIONAL_CITIES = {"Dubai", "London"};
    static final String[] TIME_SLOTS = {"04:00", "09:00", "13:00", "18:00", "22:00"};
    static List<Passenger> lastPassengers = new ArrayList<>();
    static Random random = new Random();
    static String currentUserName = "";
    static String currentUserMobile = "";

    public static void main(String[] args) throws IOException {
        initFlights();
        initHotels();
        initTaxis();
        initCards();
        while (true) {
            System.out.println("\n======================================");
            System.out.println("     AIRLINE BOOKING SYSTEM");
            System.out.println("======================================");
            System.out.println("1. User Registration");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            System.out.print("Enter Choice : ");

            String choice = readLine();

            switch (choice) {
                case "1":

                    login();

                    userMenu();

                    break;

                case "2":

                    System.out.print("Enter Admin Password : ");

                    String pass = readLine();

                    if (pass.equals("admin123")) {
                        adminPanel();
                    } else {
                        System.out.println("Invalid Admin Password.");
                    }

                    break;

                case "3":

                    System.out.println("Thank You...");
                    System.exit(0);

                default:

                    System.out.println("Invalid Choice.");
            }

        }
    }

    static void userMenu() throws IOException {
        boolean running = true;

        while (running) {
            System.out.println("\n--------- MAIN MENU ---------");
            System.out.println("1) Booking Airline");
            System.out.println("2) Booking Hotel");
            System.out.println("3) Booking Cab");
            System.out.println("4) Cancel Ticket");
            System.out.println("5) My Transactions");
            System.out.println("6) Logout");
            System.out.print("Enter Choice : ");

            String choice = readLine();

            switch (choice) {
                case "1":
                    bookAirline();
                    break;

                case "2":
                    bookHotel();
                    break;

                case "3":
                    bookTaxi();
                    break;

                case "4":
                    cancelTicket();
                    break;

                case "5":
                    DatabaseHelper.viewMyTransactions(currentUserMobile);
                    break;
                case "6":
                    running = false;
                    System.out.println("Logged Out Successfully.");
                    break;

                default:
                    System.out.println("Invalid Choice.");
            }
        }
    }

    /* ---------------------------------------------------
     *  LOGIN
     * --------------------------------------------------- */
    static void login() throws IOException {
        System.out.println("\n==============================================");
        System.out.println("            USER REGISTRATION");
        System.out.println("==============================================");

        // -------- Full Name --------
        while (true) {
            System.out.print("Enter Your Full Name : ");
            currentUserName = readLine();

            boolean valid = true;

            for (int i = 0; i < currentUserName.length(); i++) {
                char ch = currentUserName.charAt(i);

                if (!Character.isLetter(ch) && ch != ' ') {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                break;
            } else {
                System.out.println("Invalid Name!");
                System.out.println("Name should contain only alphabets and spaces.");
            }
        }
        // -------- Mobile Number --------
        while (true) {
            System.out.print("Enter Your Mobile Number (10 Digits) : ");
            currentUserMobile = readLine();

            boolean valid = true;

            if (currentUserMobile.length() != 10) {
                System.out.println("Invalid Mobile Number!");
                System.out.println("Please enter exactly 10 digits.");
                valid = false;

            } else if (currentUserMobile.charAt(0) != '6' && currentUserMobile.charAt(0) != '7' && currentUserMobile.charAt(0) != '8' && currentUserMobile.charAt(0) != '9') {
                System.out.println("Invalid Mobile Number!");
                valid = false;

            } else {
                for (int i = 0; i < currentUserMobile.length(); i++) {
                    if (!Character.isDigit(currentUserMobile.charAt(i))) {
                        System.out.println("Invalid Mobile Number!");
                        System.out.println("Mobile number should contain only digits.");
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) {
                break;
            }
        }
        // -------- Email ID --------
        String email;
        while (true) {
            System.out.print("Enter Your Email ID : ");
            email = readLine();

            if (email.contains("@") && email.contains(".")) {
                break;
            } else {
                System.out.println("Invalid Email ID!");
                System.out.println("Please enter a valid email address.");
            }
        }

        System.out.println("\n==============================================");
        System.out.println("          REGISTRATION SUCCESSFUL");
        System.out.println("==============================================");
        System.out.println("Name   : " + currentUserName);
        System.out.println("Mobile : " + currentUserMobile);
        System.out.println("Email  : " + email);
        System.out.println("==============================================");
        System.out.println("Welcome to Airline Booking System, " + currentUserName + "!\n");
        DatabaseHelper.saveUser(currentUserName, currentUserMobile, email);
        activityLog.push("LOGIN : " + currentUserName + " (" + currentUserMobile + ") at " + now());
    }

    static String now() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    /* ---------------------------------------------------
     *  INITIALIZATION (flights, hotels, taxis, cards)
     * --------------------------------------------------- */
    static void initFlights() {
        // 10 routes x 5 flights (one per time slot) = 50 flights total
        String[][] routes = {{"Ahmedabad", "Delhi", "false"}, {"Delhi", "Ahmedabad", "false"}, {"Ahmedabad", "Mumbai", "false"}, {"Mumbai", "Ahmedabad", "false"}, {"Delhi", "Mumbai", "false"}, {"Mumbai", "Delhi", "false"}, {"Delhi", "Bangalore", "false"}, {"Bangalore", "Delhi", "false"}, {"Ahmedabad", "Dubai", "true"}, {"Mumbai", "London", "true"}, {"London", "Mumbai", "true"}, {"Dubai", "London", "true"}, {"London", "Dubai", "true"}};

        int counter = 100;
        for (String[] route : routes) {
            String from = route[0], to = route[1];
            boolean intl = Boolean.parseBoolean(route[2]);
            List<Flight> list = new ArrayList<>();
            for (String slot : TIME_SLOTS) {
                counter++;
                double basePriceEco = intl ? 25000 + random.nextInt(10000) : 3000 + random.nextInt(4000);
                double basePriceBiz = basePriceEco * 2.5;
                Flight f = new Flight("AI" + counter, from, to, slot, intl, Math.round(basePriceEco * 100.0) / 100.0, Math.round(basePriceBiz * 100.0) / 100.0);
                flights.add(f);
                list.add(f);
            }
            routeMap.put(from + "-" + to, list);
        }
    }

    static void initHotels() {
        int counter = 1;
        for (String city : DOMESTIC_CITIES) {
            for (int i = 1; i <= 3; i++) {
                double price = 1500 + random.nextInt(3500);
                hotels.add(new Hotel("H" + (counter++), city + " Hotel " + i, city, price));
            }
        }
        for (String city : INTERNATIONAL_CITIES) {
            for (int i = 1; i <= 3; i++) {
                double price = 6000 + random.nextInt(6000);
                hotels.add(new Hotel("H" + (counter++), city + " Hotel " + i, city, price));
            }
        }
    }

    static void initTaxis() {
        int counter = 1;
        String[] driverNames = {"Ramesh", "Suresh", "Amit", "Vijay", "Karan", "Rohit", "Sanjay", "Manoj", "Deepak", "Ajay"};
        for (String city : DOMESTIC_CITIES) {
            for (int i = 0; i < 10; i++) {
                String mobile = "9" + (100000000 + random.nextInt(899999999));
                taxis.add(new Taxi("T" + (counter++), driverNames[i % driverNames.length] + " " + (i + 1), city, mobile));
            }
        }
    }

    static void initCards() {

        validCards.put("1111222233334444", new BankCard("1111222233334444", "TEST USER ONE", "111", false, "HDFC"));

        validCards.put("5555666677778888", new BankCard("5555666677778888", "TEST USER TWO", "222", true, "SBI"));

        validCards.put("9999000011112222", new BankCard("9999000011112222", "TEST USER THREE", "333", false, "ICICI"));

        validCards.put("4444333322221111", new BankCard("4444333322221111", "TEST USER FOUR", "444", true, "ICICI"));
    }

    static void showAvailableSeats(Flight f, String seatType) {


        if (seatType.equalsIgnoreCase("Business")) {
            showBusinessSeatMap(f);
        } else {
            showEconomySeatMap(f);
        }

        System.out.println();
        System.out.println("[ ] = Available");
        System.out.println("[X] = Already Booked");
        System.out.println();
    }

    static void showBusinessSeatMap(Flight f) {
        System.out.println("=============== BUSINESS CLASS ===============");
        System.out.println();
        System.out.println("          A       C          D       F");
        System.out.println("               LEFT   AISLE   RIGHT");
        System.out.println("----------------------------------------------");

        // 10 business seats = 5 rows x 2 seats
        for (int row = 1; row <= 5; row++) {
            String leftSeat = row + "A";
            String rightSeat = row + "F";

            String leftStatus = f.bookedSeats.contains(leftSeat) ? "[X]" : "[ ]";

            String rightStatus = f.bookedSeats.contains(rightSeat) ? "[X]" : "[ ]";

            System.out.printf("Row %-2d     %s %-3s       | AISLE |       %s %-3s%n", row, leftStatus, leftSeat, rightStatus, rightSeat);
        }

        System.out.println("----------------------------------------------");
        System.out.println("A = Window Seat");
        System.out.println("F = Window Seat");
    }

    static void showEconomySeatMap(Flight f) {
        System.out.println("================ ECONOMY CLASS =================");
        System.out.println();
        System.out.println("        A     B     C          D     E     F");
        System.out.println("       WINDOW       AISLE             WINDOW");
        System.out.println("------------------------------------------------");

        int seatCount = 0;

        for (int row = 1; row <= 9; row++) {
            System.out.printf("%-3d   ", row);

            char[] letters = {'A', 'B', 'C', 'D', 'E', 'F'};

            for (char letter : letters) {
                if (seatCount >= 50) {
                    System.out.print("      ");
                    continue;
                }

                String seat = row + String.valueOf(letter);

                if (f.bookedSeats.contains(seat)) {
                    System.out.print("[X]   ");
                } else {
                    System.out.print("[ ]   ");
                }

                seatCount++;

                if (letter == 'C') {
                    System.out.print("  | |   ");
                }
            }

            System.out.println();
        }

        System.out.println("------------------------------------------------");
        System.out.println("A / F = Window Seats");
        System.out.println("B / E = Middle Seats");
        System.out.println("C / D = Aisle Seats");
    }

    static String getSeatPosition(String seatNumber) {
        char letter = Character.toUpperCase(seatNumber.charAt(seatNumber.length() - 1));

        if (letter == 'A' || letter == 'F') {
            return "Window";
        } else if (letter == 'C' || letter == 'D') {
            return "Aisle";
        } else {
            return "Middle";
        }
    }

    static boolean isValidSeat(String seat, String seatType) {
        seat = seat.toUpperCase();

        if (seatType.equalsIgnoreCase("Business")) {
            // Business: 1A-5A and 1F-5F
            return seat.matches("[1-5][AF]");
        } else {
            // Economy: rows 1-8, A-F = 48 seats
            if (seat.matches("[1-8][A-F]")) {
                return true;
            }

            // Last two economy seats
            return seat.matches("9[AB]");
        }
    }

    /* ---------------------------------------------------
     *  1) FLIGHT BOOKING
     * --------------------------------------------------- */
    static void bookAirline() throws IOException {
        System.out.println("\n========== AIRLINE BOOKING ==========");
        System.out.println("1) Domestic Flight");
        System.out.println("2) International Flight");
        System.out.print("Enter Your Choice : ");
        String flightType = readLine();

        if (flightType.equals("1")) {
            System.out.println("\n----- Domestic Flight -----");
            System.out.println("1) One Way Trip");
            System.out.println("2) Round Trip");
            System.out.println("3) City Drop Trip");
            System.out.print("Enter Choice : ");
        } else if (flightType.equals("2")) {
            System.out.println("\n----- International Flight -----");
            System.out.println("1) One Way Trip");
            System.out.println("2) Round Trip");
            System.out.println("3) City Drop Trip");
            System.out.print("Enter Choice : ");
        } else {
            System.out.println("Invalid Choice.");
            return;
        }

        String choice = readLine();

        switch (choice) {
            case "1": {
                Flight f = selectFlightFlow(flightType.equals("2"));
                if (f != null) finalizeFlightBooking(f);
                break;
            }

            case "2": {
                System.out.println("--- Onward Flight ---");
                Flight onward = selectFlightFlow(flightType.equals("2"));

                if (onward == null) return;

                String returnDate;
                while (true) {
                    System.out.print("\nEnter Return Date (dd-MM-yyyy): ");
                    returnDate = readLine();

                    try {
                        LocalDate rDate = LocalDate.parse(returnDate, DATE_FORMATTER);
                        LocalDate today = LocalDate.now();
                        LocalDate onwardDate = LocalDate.parse(lastDate, DATE_FORMATTER);

                        if (rDate.isBefore(today)) {
                            System.out.println("Invalid Date!");
                            System.out.println("Return date cannot be in the past.");
                        } else if (rDate.isBefore(onwardDate)) {
                            System.out.println("Invalid Date!");
                            System.out.println("Return date cannot be before onward flight date (" + lastDate + ").");
                        } else if (rDate.isAfter(today.plusYears(5))) {
                            System.out.println("Ourservices is not avalable for this long future");
                        } else if (rDate.isAfter(today.plusYears(2))) {
                            System.out.println("Invalid Date! Booking is only allowed up to 2 years in advance.");
                        } else {
                            break;
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid Date Format!");
                        System.out.println("Please enter date in dd-MM-yyyy format.");
                    }
                }

                System.out.println("\n--- Return Flight ---");

                Flight ret = selectReturnFlight(onward, returnDate);

                if (ret == null) return;

                finalizeRoundTripBooking(onward, ret);
                break;
            }

            case "3": {
                System.out.println("--- Leg 1 (First City to Stopover City) ---");
                Flight leg1 = selectFlightFlow(flightType.equals("2"));
                if (leg1 == null) return;

                System.out.println("--- Leg 2 (Stopover City to Final Destination) ---");
                Flight leg2 = selectFlightFlow(flightType.equals("2"));
                if (leg2 == null) return;

                finalizeRoundTripBooking(leg1, leg2);
                break;
            }

            default:
                System.out.println("Invalid Choice.");
        }
    }

    static void printFlightTable(List<Flight> flightList, String seatType) {
        if (flightList == null || flightList.isEmpty()) {
            System.out.println("No flights available.");
            return;
        }

        boolean isBusiness = "Business".equalsIgnoreCase(seatType);
        String classHeader = isBusiness ? "Business Class" : "Economy Class";

        System.out.printf("| %-3s | %-9s | %-25s | %-9s | %-13s | %-30s |%n", "No.", "Flight ID", "Route", "Time Slot", "Type", classHeader);

        for (int i = 0; i < flightList.size(); i++) {
            Flight f = flightList.get(i);
            String routeStr = f.from + " -> " + f.to;
            String typeStr = f.international ? "International" : "Domestic";

            String classStr = isBusiness ? String.format("Rs.%.2f (%d left)", f.businessPrice, f.emptyBusinessSeats()) : String.format("Rs.%.2f (%d left)", f.economyPrice, f.emptyEconomySeats());

            System.out.printf("| %-3d | %-9s | %-25s | %-9s | %-13s | %-30s |%n", (i + 1), f.flightId, routeStr, f.timeSlot, typeStr, classStr);
        }
    }

    static void selectPassengerSeats(Flight flight, String seatType) throws IOException {
        Set<String> selectedNow = new HashSet<>();

        System.out.println("\n==========================================");
        System.out.println("             SEAT SELECTION");
        System.out.println("==========================================");

        for (int i = 0; i < lastPassengers.size(); i++) {
            Passenger passenger = lastPassengers.get(i);

            while (true) {
                // Show updated map before every passenger chooses
                showAvailableSeats(flight, seatType);

                System.out.println("\nPassenger " + (i + 1) + " : " + passenger.name);

                System.out.print("Enter your preferred Seat Number: ");

                String seat = readLine().trim().toUpperCase();

                // Check whether seat exists
                if (!isValidSeat(seat, seatType)) {
                    System.out.println("\nInvalid Seat Number!");

                    if (seatType.equalsIgnoreCase("Business")) {
                        System.out.println("Please select a valid Business Class seat.");
                    } else {
                        System.out.println("Please select a valid Economy Class seat.");
                    }

                    continue;
                }

                // Check seat already booked
                if (flight.bookedSeats.contains(seat)) {
                    System.out.println("\nSeat " + seat + " is already booked.");

                    System.out.println("Please choose another available seat.");

                    continue;
                }

                // Check another passenger selected same seat
                if (selectedNow.contains(seat)) {
                    System.out.println("\nSeat " + seat + " is already selected by another passenger.");

                    continue;
                }
                // Save passenger's seat
                passenger.seatNumber = seat;
                passenger.seatPosition = getSeatPosition(seat);
                // Mark seat immediately
                selectedNow.add(seat);
                flight.bookedSeats.add(seat);
                System.out.println("\nSeat Selected Successfully!");
                System.out.println("--------------------------------");
                System.out.println("Passenger : " + passenger.name);
                System.out.println("Seat      : " + passenger.seatNumber);
                System.out.println("Position  : " + passenger.seatPosition);
                System.out.println("--------------------------------");

                break;
            }
        }

        // Permanently mark selected seats as booked
        flight.bookedSeats.addAll(selectedNow);


        // Show final selection
        System.out.println("\n==========================================");
        System.out.println("           SELECTED SEATS");
        System.out.println("==========================================");

        for (Passenger p : lastPassengers) {
            System.out.printf("%-20s -> %-4s (%s)%n", p.name, p.seatNumber, p.seatPosition);
        }

        System.out.println("==========================================");
    }

    static Flight selectReturnFlight(Flight onward, String returnDate) throws IOException {
        String from = onward.to;
        String to = onward.from;

        System.out.println("Return Route : " + from + " -> " + to);
        System.out.println("Return Date  : " + returnDate);

        ArrayList<Flight> list = new ArrayList<>();

        for (Flight f : flights) {
            if (f.from.equalsIgnoreCase(from) && f.to.equalsIgnoreCase(to)) {
                list.add(f);
            }
        }

        if (list.isEmpty()) {
            System.out.println("No return flights available.");
            return null;
        }

        System.out.println("\n================ RETURN FLIGHTS ================");
        System.out.println("Route : " + from + " -> " + to);
        System.out.println("Date  : " + returnDate);
        System.out.println("================================================");

        System.out.println("No\tFlightID\tRoute\t\t\tTime\tType\t\tPrice");

        System.out.println("--------------------------------------------------------------------------------");

        for (int i = 0; i < list.size(); i++) {
            Flight f = list.get(i);
            double price;
            if (lastSeatType.equals("Economy")) {
                price = f.economyPrice;
            } else {
                price = f.businessPrice;
            }
            System.out.println((i + 1) + "\t" + f.flightId + "\t\t" + f.from + "->" + f.to + "\t\t" + f.timeSlot + "\t" + (f.international ? "International" : "Domestic") + "\t" + "Rs." + price);
        }

        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("-----------------------------------------------------------------------------------------------");

        System.out.print("Select Return Flight Number : ");

        int ch;

        try {
            ch = Integer.parseInt(readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Flight.");
            return null;
        }

        if (ch < 1 || ch > list.size()) {
            System.out.println("Invalid Flight.");
            return null;
        }

        return list.get(ch - 1);
    }

    /**
     * Handles: pickup/drop/date/day/persons/class/route display/flight select/seat select. Returns chosen flight (with a seat reserved) or null.
     */
    static Flight selectFlightFlow(boolean international) throws IOException {
        // ---------------- Pickup Location ----------------
        String from = "";

        while (true) {
            System.out.println("\n========== PICKUP LOCATION ==========");

            if (!international) {
                System.out.println("1. Ahmedabad");
                System.out.println("2. Delhi");
                System.out.println("3. Mumbai");
                System.out.println("4. Bangalore");
            } else {
                System.out.println("1. Ahmedabad");
                System.out.println("2. Mumbai");
                System.out.println("3. Dubai");
                System.out.println("4. London");
            }

            System.out.print("Enter Choice : ");
            String choice = readLine();

            if (!international) {
                switch (choice) {
                    case "1":
                        from = "Ahmedabad";
                        break;
                    case "2":
                        from = "Delhi";
                        break;
                    case "3":
                        from = "Mumbai";
                        break;
                    case "4":
                        from = "Bangalore";
                        break;

                    default:
                        System.out.println("Invalid Choice! Please try again.");
                        continue;
                }
            } else {
                switch (choice) {
                    case "1":
                        from = "Ahmedabad";
                        break;
                    case "2":
                        from = "Mumbai";
                        break;
                    case "3":
                        from = "Dubai";
                        break;
                    case "4":
                        from = "London";
                        break;

                    default:
                        System.out.println("Invalid Choice! Please try again.");
                        continue;
                }
            }

            break;
        }


// ---------------- Drop Location ----------------
        String to = "";

        while (true) {
            System.out.println("\n========== DROP LOCATION ==========");

            if (!international) {
                System.out.println("1. Ahmedabad");
                System.out.println("2. Delhi");
                System.out.println("3. Mumbai");
                System.out.println("4. Bangalore");
            } else {
                System.out.println("1. Ahmedabad");
                System.out.println("2. Mumbai");
                System.out.println("3. Dubai");
                System.out.println("4. London");
            }

            System.out.print("Enter Choice : ");
            String choice = readLine();

            if (!international) {
                switch (choice) {
                    case "1":
                        to = "Ahmedabad";
                        break;
                    case "2":
                        to = "Delhi";
                        break;
                    case "3":
                        to = "Mumbai";
                        break;
                    case "4":
                        to = "Bangalore";
                        break;

                    default:
                        System.out.println("Invalid Choice! Please try again.");
                        continue;
                }
            } else {
                switch (choice) {
                    case "1":
                        to = "Ahmedabad";
                        break;
                    case "2":
                        to = "Mumbai";
                        break;
                    case "3":
                        to = "Dubai";
                        break;
                    case "4":
                        to = "London";
                        break;

                    default:
                        System.out.println("Invalid Choice! Please try again.");
                        continue;
                }
            }

            if (from.equals(to)) {
                System.out.println("Pickup and Drop locations cannot be the same.");
                continue;
            }

            break;
        }


// ---------------- Booking Date ----------------
        String date;
        String day;
        while (true) {
            System.out.print("Enter Booking Date (dd-MM-yyyy): ");
            date = readLine();

            try {
                LocalDate bookingDate = LocalDate.parse(date, DATE_FORMATTER);
                LocalDate today = LocalDate.now();

                if (bookingDate.isBefore(today)) {
                    System.out.println("Invalid Date!");
                    System.out.println("Booking date cannot be in the past.");
                } else if (bookingDate.isAfter(today.plusYears(5))) {
                    System.out.println("ourservices is not avalable for this long future");
                } else if (bookingDate.isAfter(today.plusYears(2))) {
                    System.out.println("Invalid Date! Booking is only allowed up to 2 years in advance.");
                } else {
                    DayOfWeek dayOfWeek = bookingDate.getDayOfWeek();
                    day = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                    System.out.println("Day : " + day);
                    break;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid Date Format!");
                System.out.println("Please enter date in dd-MM-yyyy format.");
            }
        }
        int persons;

        while (true) {
            System.out.print("Enter total no. of persons (Maximum 10): ");
            String personsStr = readLine();

            try {
                persons = Integer.parseInt(personsStr);

                if (persons <= 0) {
                    System.out.println("Number of persons must be greater than 0.");
                } else if (persons > 10) {
                    System.out.println("Maximum 10 tickets can be booked at a time.");
                    System.out.println("Please enter between 1 and 10 persons.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }

// Clear old passenger data
        lastPassengers.clear();


// Ask details of every passenger
        for (int i = 1; i <= persons; i++) {
            System.out.println("\n======================================");
            System.out.println("       PASSENGER " + i + " DETAILS");
            System.out.println("======================================");

            // -------- Passenger Name --------
            String passengerName;

            while (true) {
                System.out.print("Enter Passenger Name : ");
                passengerName = readLine();

                if (passengerName.matches("^[A-Za-z ]+$")) {
                    break;
                }

                System.out.println("Invalid Name!");
                System.out.println("Name should contain only alphabets and spaces.");
            }


            // -------- Passenger Age --------
            int age;

            while (true) {
                System.out.print("Enter Age : ");

                try {
                    age = Integer.parseInt(readLine());

                    if (age >= 1 && age <= 120) {
                        break;
                    }

                    System.out.println("Age must be between 1 and 120.");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid Age! Please enter numbers only.");
                }
            }


            // -------- Passenger Gender --------
            String gender;

            label:
            while (true) {
                System.out.println("Select Gender:");
                System.out.println("1. Male");
                System.out.println("2. Female");
                System.out.println("3. Other");
                System.out.print("Enter Choice : ");

                String genderChoice = readLine();

                switch (genderChoice) {
                    case "1":
                        gender = "Male";
                        break label;
                    case "2":
                        gender = "Female";
                        break label;
                    case "3":
                        gender = "Other";
                        break label;
                    default:
                        System.out.println("Invalid choice! Enter 1, 2 or 3.");
                        break;
                }
            }


            // Store passenger
            lastPassengers.add(new Passenger(passengerName, age, gender));

            System.out.println("Passenger " + i + " details added successfully.");
        }
        String classChoice;
        String seatType;

        while (true) {
            System.out.print("Choose class (1-Business / 2-Economy): ");
            classChoice = readLine();

            if (classChoice.equals("1")) {
                seatType = "Business";
                break;
            } else if (classChoice.equals("2")) {
                seatType = "Economy";
                break;
            } else {
                System.out.println("Invalid choice! Please enter only 1 or 2.");
            }
        }
        List<Flight> options = routeMap.get(from + "-" + to);
        if (options == null) {
            System.out.println("No flights found for this route.");
            return null;
        }

        System.out.println("\n================ AVAILABLE FLIGHTS ================");
        System.out.println("Route : " + from + " -> " + to);
        System.out.println("Date  : " + date + " (" + day + ")");
        System.out.println("===================================================");

        if (seatType.equals("Economy")) {
            System.out.println("No\tFlightID\tRoute\t\t\tTime\tType\t\tPrice");
        } else {
            System.out.println("No\tFlightID\tRoute\t\t\tTime\tType\t\tPrice");
        }

        System.out.println("--------------------------------------------------------------------------------");

        List<Flight> activeOptions = new ArrayList<>();

        for (Flight f : options) {

            if (f.cancelled || cancelledFlightIds.contains(f.flightId)) {
                continue;
            }

            activeOptions.add(f);

            double price;

            if (seatType.equals("Economy")) {
                price = f.economyPrice;
            } else {
                price = f.businessPrice;
            }

            System.out.println(activeOptions.size() + "\t" + f.flightId + "\t\t" + f.from + "->" + f.to + "\t\t" + f.timeSlot + "\t" + (f.international ? "International" : "Domestic") + "\t" + "Rs." + price);
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("---------------------------------------------------------------------------------");

        if (activeOptions.isEmpty()) {
            System.out.println("No active flights currently available on this route.");
            return null;
        }
        System.out.print("Select flight number from the list above: ");
        String sel = readLine();
        int idx;
        try {
            idx = Integer.parseInt(sel) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return null;
        }
        if (idx < 0 || idx >= activeOptions.size()) {
            System.out.println("Invalid selection.");
            return null;
        }
        Flight chosen = activeOptions.get(idx);
        int available = seatType.equals("Business") ? chosen.emptyBusinessSeats() : chosen.emptyEconomySeats();

        if (available == 0) {
            System.out.println("\nNo seats available.");
            System.out.println("All " + persons + " passenger(s) added to Waiting Queue.");

            for (int i = 1; i <= persons; i++) {
                waitingQueue.offer(currentUserName + " - " + chosen.flightId + " - " + seatType);
            }

            return null;
        }

        if (persons > available) {
            System.out.println("\n======================================");
            System.out.println("       INSUFFICIENT SEATS");
            System.out.println("======================================");
            System.out.println("Tickets Requested : " + persons);
            System.out.println("Seats Available   : " + available);
            System.out.println("======================================");

            while (true) {
                System.out.print("Would you like to join the Waiting Queue? (Y/N): ");
                String choice = readLine();

                if (choice.equalsIgnoreCase("Y")) {
                    for (int i = 0; i < persons; i++) {
                        waitingQueue.addLast(currentUserName + " - " + chosen.flightId + " - " + seatType);
                    }

                    System.out.println("\n" + persons + " passenger(s) added to the Waiting Queue.");

                    System.out.println("No payment is required until seats become available.");

                    return null;
                } else if (choice.equalsIgnoreCase("N")) {
                    System.out.println("\nBooking cancelled.");
                    System.out.println("No passengers were added to the Waiting Queue.");

                    return null;
                } else {
                    System.out.println("Invalid choice! Please enter Y or N.");
                }
            }
        }
        // reserve 'persons' seats
        selectPassengerSeats(chosen, seatType);
        chosen.timeSlot = chosen.timeSlot; // (kept for clarity; date/day are booking-specific, stored in ticket details)
        lastSeatType = seatType;
        lastPersons = persons;
        lastDate = date;
        lastDay = day;
        return chosen;
    }


    // small holder fields used right after selectFlightFlow() (simple approach, avoids extra DTO class)
    static String lastSeatType, lastDate, lastDay;
    static int lastPersons;

    static void finalizeFlightBooking(Flight f) throws IOException {
        double base = lastSeatType.equals("Business") ? f.businessPrice : f.economyPrice;
        double originalAmount = base * lastPersons;

        double afterStudentDiscount = applyStudentDiscountIfAny(originalAmount);

        double taxAmount = afterStudentDiscount * taxRate;

        double total = afterStudentDiscount + taxAmount;

        System.out.println("\n========== BILL SUMMARY ==========");
        System.out.printf("Amount After Student Discount : Rs. %.2f%n", afterStudentDiscount);
        System.out.printf("Tax (%.0f%%)                  : Rs. %.2f%n", taxRate * 100, taxAmount);
        System.out.printf("Amount Before Card Discount   : Rs. %.2f%n", total);
        System.out.println("==================================");

        double finalAmount = payWithCard(total);

        if (finalAmount == -1) {
            System.out.println("Booking Cancelled.");
            return;
        }

        String details = "Flight " + f.flightId + " (" + f.from + " -> " + f.to + "), " + lastSeatType + " class, " + lastPersons + " pax, Date: " + lastDate + " (" + lastDay + ")" + (f.international ? " [International]" : " [Domestic]");

        issueTicket("FLIGHT", details, finalAmount);
    }

    static void finalizeRoundTripBooking(Flight onward, Flight returnFlight) throws IOException {

        double base1 = lastSeatType.equals("Business") ? onward.businessPrice : onward.economyPrice;

        double base2 = lastSeatType.equals("Business") ? returnFlight.businessPrice : returnFlight.economyPrice;

        double originalAmount = (base1 + base2) * lastPersons;

        double afterStudentDiscount = applyStudentDiscountIfAny(originalAmount);

        double taxAmount = afterStudentDiscount * taxRate;

        double total = afterStudentDiscount + taxAmount;

        System.out.println("\n========== ROUND TRIP BILL ==========");
        System.out.println("Amount : Rs." + total);
        System.out.println("=====================================");

        double finalAmount = payWithCard(total);

        if (finalAmount == -1) {
            System.out.println("Booking Cancelled.");
            return;
        }

        // ONWARD FLIGHT
        String onwardDetails = "Flight " + onward.flightId + " (" + onward.from + " -> " + onward.to + "), " + lastSeatType + " class, " + lastPersons + " pax";

        // RETURN FLIGHT
        String returnDetails = "Flight " + returnFlight.flightId + " (" + returnFlight.from + " -> " + returnFlight.to + "), " + lastSeatType + " class, " + lastPersons + " pax";

        double amountPerLeg = finalAmount / 2;

        // IMPORTANT
        issueTicket("FLIGHT", onwardDetails, amountPerLeg);

        issueTicket("FLIGHT", returnDetails, amountPerLeg);
    }

    static double applyStudentDiscountIfAny(double amount) throws IOException {
        System.out.print("Are you a student? (y/n): ");
        String ans = readLine();

        if (ans.equalsIgnoreCase("y")) {
            String id;

            while (true) {
                System.out.print("Enter Student ID (Example: A1234): ");
                id = readLine().trim();

                // First letter alphabet + 4 digits
                if (id.matches("[A-Za-z][0-9]{4}")) {
                    break;
                }

                System.out.println("Invalid Student ID!");
                System.out.println("Student ID must be in the format A1234.");
            }

            int discountPercent = 5 + random.nextInt(6);
            double discounted = amount - (amount * discountPercent / 100.0);

            System.out.println("\n========== STUDENT DISCOUNT ==========");
            System.out.println("Student ID Verified.");

            double discountAmount = amount * discountPercent / 100.0;

            System.out.printf("Original Amount        : Rs. %.2f%n", amount);
            System.out.println("Student Discount       : " + discountPercent + "%");
            System.out.printf("Discount Amount        : Rs. %.2f%n", discountAmount);
            System.out.printf("Amount After Discount  : Rs. %.2f%n", discounted);
            System.out.println("======================================");

            return discounted;
        }

        return amount;
    }

    /* ---------------------------------------------------
     *  2) HOTEL BOOKING
     * --------------------------------------------------- */
    static void bookHotel() throws IOException {
        System.out.print("Enter city: ");
        String city = readLine();

        List<Hotel> cityHotels = new ArrayList<>();
        System.out.println("Hotels in " + city + ":");
        for (Hotel h : hotels) {
            if (h.city.equalsIgnoreCase(city)) {
                cityHotels.add(h);
                System.out.println(cityHotels.size() + ") " + h);
            }
        }
        if (cityHotels.isEmpty()) {
            System.out.println("No hotels found in this city.");
            return;
        }
        System.out.print("Select hotel number: ");
        int idx;
        try {
            idx = Integer.parseInt(readLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (idx < 0 || idx >= cityHotels.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Hotel chosen = cityHotels.get(idx);

        System.out.print("Enter total days to stay: ");
        int days;
        try {
            days = Integer.parseInt(readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number of days.");
            return;
        }

        double total = chosen.pricePerNight * days;
        total += total * taxRate;
        double finalAmount = payWithCard(total);

        if (finalAmount == -1) {
            System.out.println("Hotel Booking Cancelled.");
            return;
        }
        String details = chosen.name + " (" + chosen.city + "), " + days + " night(s) @ Rs." + chosen.pricePerNight + "/night";

        issueTicket("HOTEL", details, finalAmount);
    }

    /* ---------------------------------------------------
     *  3) TAXI (CAB) BOOKING
     * --------------------------------------------------- */
    static void bookTaxi() throws IOException {
        System.out.print("Enter city: ");
        String city = readLine();

        List<Taxi> cityTaxis = new ArrayList<>();
        System.out.println("Available taxis in " + city + ":");
        for (Taxi t : taxis) {
            if (t.city.equalsIgnoreCase(city)) {
                cityTaxis.add(t);
                System.out.println(cityTaxis.size() + ") " + t);
            }
        }
        if (cityTaxis.isEmpty()) {
            System.out.println("No taxis found in this city.");
            return;
        }
        System.out.print("Select taxi number: ");
        int idx;
        try {
            idx = Integer.parseInt(readLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (idx < 0 || idx >= cityTaxis.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Taxi chosen = cityTaxis.get(idx);

        System.out.print("Enter distance in km: ");
        double km;
        try {
            km = Double.parseDouble(readLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid distance.");
            return;
        }

        double total = km * chosen.perKmPrice;
        total += total * taxRate;
        double finalAmount = payWithCard(total);

        if (finalAmount == -1) {
            System.out.println("Taxi Booking Cancelled.");
            return;
        }
        String details = "Taxi " + chosen.taxiId + " - Driver " + chosen.name + " (" + chosen.mobile + "), " + km + " km @ Rs." + chosen.perKmPrice + "/km";

        issueTicket("TAXI", details, finalAmount);
    }

    /* ---------------------------------------------------
     *  PAYMENT (card only) with premium / bank discounts
     * --------------------------------------------------- */
    static double payWithCard(double amount) throws IOException {
        System.out.println("\n--- Payment (Card only) ---");
        System.out.println("Amount before card discount: Rs." + String.format("%.2f", amount));

        // ---------------- Card Number: must be exactly 16 digits, re-ask until valid ----------------
        String cardNumber;
        while (true) {
            System.out.print("Enter card number (16 digits): ");
            cardNumber = readLine();

            if (cardNumber.matches("^[0-9]{16}$")) {
                break;
            } else {
                System.out.println("Invalid Card Number!");
                System.out.println("Card number must be exactly 16 digits (0-9 only).");
            }
        }

        // ---------------- CVV: must be exactly 3 digits, re-ask until valid ----------------
        String cvv;
        while (true) {
            System.out.print("Enter CVV (3 digits): ");
            cvv = readLine();

            if (cvv.matches("^[0-9]{3}$")) {
                break;
            } else {
                System.out.println("Invalid CVV!");
                System.out.println("CVV must be exactly 3 digits.");
            }
        }

        // ---------------- Name on Card: alphabets (and spaces) only, re-ask until valid ----------------
        String name;
        while (true) {
            System.out.print("Enter name on card: ");
            name = readLine();

            if (name.matches("^[A-Za-z ]+$")) {
                break;
            } else {
                System.out.println("Invalid Name!");
                System.out.println("Name should contain only alphabets and spaces.");
            }
        }

        System.out.println("\nCard Verified Successfully.");

        // Any correctly-formatted 16-digit card is accepted for payment now.
        // validCards is kept only as a small lookup of "known" demo/premium bank
        // cards so the premium % / ICICI % discount logic still has something to
        // demonstrate; a brand-new card just gets no discount instead of being rejected.
        BankCard card = validCards.get(cardNumber);
        boolean premium = (card != null) && card.premium;
        String bank = (card != null) ? card.bank : "OTHER";
        DatabaseHelper.saveBankCard(cardNumber, name, cvv, bank, premium);
        double premiumDiscount = 0;
        double bankDiscount = 0;

        if (premium) {
            premiumDiscount = 1 + random.nextInt(10);
        }

        if (bank.equalsIgnoreCase("ICICI")) {
            bankDiscount = 10;
        }

        double bestDiscountPercent = Math.max(premiumDiscount, bankDiscount);

        if (bestDiscountPercent > 0) {
            System.out.println("Best Card Discount Applied : " + bestDiscountPercent + "%");
        } else {
            System.out.println("No Card Discount Available.");
        }

        double originalAmount = amount;
        double discountAmount = 0;

        if (bestDiscountPercent > 0) {
            discountAmount = originalAmount * bestDiscountPercent / 100.0;
            amount = originalAmount - discountAmount;
        }

        System.out.println("--------------------------------");
        System.out.printf("Amount Before Card Discount : Rs. %.2f%n", originalAmount);
        System.out.printf("Card Discount               : %.0f%%%n", bestDiscountPercent);
        System.out.printf("Discount Amount             : Rs. %.2f%n", discountAmount);
        System.out.printf("Final Amount To Pay         : Rs. %.2f%n", amount);
        System.out.println("--------------------------------");

        System.out.println("Processing payment, please wait...");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
        System.out.println("Payment Successful! Amount charged: Rs." + String.format("%.2f", amount));

        String maskedCard = "XXXXXXXXXXXX" + cardNumber.substring(12);

        DatabaseHelper.savePayment(currentUserMobile, currentUserName, maskedCard, bank, premium, originalAmount, bestDiscountPercent, amount, now());

// Show user's total transactions after successful payment
        System.out.println("\n=============================================");
        System.out.println("          MY TRANSACTION SUMMARY");
        System.out.println("=============================================");

        DatabaseHelper.viewMyTransactions(currentUserMobile);

        System.out.println("=============================================");

        return amount;
    }

    static String downloadTicket(String code, String type, String passengerName, int age, String gender, String details, double amount) {

        try {
            File folder = new File("tickets");

            if (!folder.exists()) {
                folder.mkdir();
            }

            String filePath = "tickets/" + code + ".txt";

            PrintWriter writer = new PrintWriter(new FileWriter(filePath));

            writer.println("==================================================");
            writer.println("                 AIRLINE TICKET");
            writer.println("==================================================");
            writer.println();
            writer.println("Ticket Code      : " + code);
            writer.println("Booking User     : " + currentUserName);
            writer.println("Mobile Number    : " + currentUserMobile);
            writer.println();
            writer.println("Passenger Name   : " + passengerName);
            writer.println("Passenger Age    : " + age);
            writer.println("Passenger Gender : " + gender);
            writer.println();
            writer.println("Booking Type     : " + type);
            writer.println("Details          : " + details);
            writer.println();
            writer.printf("Amount Paid      : Rs. %.2f%n", amount);
            writer.println("Booking Time     : " + now());
            writer.println("Status           : CONFIRMED");
            writer.println();
            writer.println("==================================================");
            writer.println("       Thank You For Booking With Us");
            writer.println("==================================================");

            writer.close();

            System.out.println();
            System.out.println("Ticket Downloaded Successfully!");
            System.out.println("Ticket File : " + new File(filePath).getAbsolutePath());

            return filePath;

        } catch (IOException e) {
            System.out.println("Error downloading ticket: " + e.getMessage());
            return null;
        }
    }

    /* ---------------------------------------------------
     *  TICKET ISSUE / CANCEL
     * --------------------------------------------------- */
    static void issueTicket(String type, String details, double amount) {

        // ================= FLIGHT TICKET =================
        if (type.startsWith("FLIGHT") && !lastPassengers.isEmpty()) {

            double amountPerPassenger = amount / lastPassengers.size();

            for (Passenger p : lastPassengers) {

                String code = "TCK" + (System.currentTimeMillis() % 100000) + (100 + random.nextInt(900));

                Ticket t = new Ticket(code, type, currentUserMobile, p.name, details, amountPerPassenger, now());

                tickets.add(t);

                activityLog.push("BOOKING: " + type + " " + code + " Passenger: " + p.name + " at " + now());

                // ================= PRINT TICKET =================

                System.out.println("\n===================== TICKET =====================");
                System.out.println("Ticket Code      : " + code);
                System.out.println("Booking User     : " + currentUserName);
                System.out.println("Mobile           : " + currentUserMobile);
                System.out.println("Passenger Name   : " + p.name);
                System.out.println("Passenger Age    : " + p.age);
                System.out.println("Passenger Gender : " + p.gender);
                System.out.println("Seat Number      : " + p.seatNumber);
                System.out.println("Seat Position    : " + p.seatPosition);
                System.out.println("Type             : " + type);
                System.out.println("Details          : " + details);
                System.out.println("Amount Paid      : Rs." + String.format("%.2f", amountPerPassenger));
                System.out.println("Booking Time     : " + now());
                System.out.println("Status           : CONFIRMED");
                System.out.println("==================================================");
                // ================= DOWNLOAD TICKET =================
                String ticketFile = downloadTicket(code, type, p.name, p.age, p.gender, details, amountPerPassenger);
                // ================= SAVE INTO DATABASE =================
                DatabaseHelper.saveTicket(code, type, currentUserMobile, currentUserName, p.name, p.age, p.gender, details, amountPerPassenger, now(), ticketFile);
            } // VERY IMPORTANT: closes for loop
        } else {
            // ================= HOTEL / TAXI =================
            String code = "TCK" + (System.currentTimeMillis() % 100000) + (100 + random.nextInt(900));
            Ticket t = new Ticket(code, type, currentUserMobile, currentUserName, details, amount, now());
            tickets.add(t);
            t.print();
            // Download Hotel / Taxi ticket
            String ticketFile = downloadTicket(code, type, currentUserName, 0, "N/A", details, amount);
            // Save Hotel / Taxi ticket into database
            DatabaseHelper.saveTicket(code, type, currentUserMobile, currentUserName, currentUserName, 0, "N/A", details, amount, now(), ticketFile);
        }
    }

    static void cancelTicket() throws IOException {
        System.out.print("Enter your mobile number: ");
        String mobile = readLine();
        System.out.print("Enter ticket unique code: ");
        String code = readLine();
        for (Ticket t : tickets) {
            if (t.ticketCode.equals(code) && t.userMobile.equals(mobile)) {
                if (t.cancelled) {
                    System.out.println("This ticket is already cancelled.");
                    return;
                }
                t.cancelled = true;
                int cancellationPercent = 1 + random.nextInt(3);
                double cancellationCharge = t.amountPaid * cancellationPercent / 100.0;
                double refundAmount = t.amountPaid - cancellationCharge;
                System.out.println("Ticket " + code + " cancelled successfully.");
                System.out.println("\n========== CANCELLATION SUMMARY ==========");
                System.out.println("Ticket Code         : " + t.ticketCode);
                System.out.printf("Original Amount     : Rs. %.2f%n", t.amountPaid);
                System.out.println("Cancellation Charge : " + cancellationPercent + "%");
                System.out.printf("Amount Deducted     : Rs. %.2f%n", cancellationCharge);
                System.out.printf("Refund Amount       : Rs. %.2f%n", refundAmount);
                System.out.println("Status              : CANCELLED");
                System.out.println("==========================================");
                // Update database
                DatabaseHelper.cancelTicket(code);
                // Remove booked seat
                DatabaseHelper.deleteBookedSeat(code);
                return;
            }
        }
        System.out.println("No matching ticket found for that mobile number and code.");
    }

    /* ---------------------------------------------------
     *  ADMIN PANEL
     * --------------------------------------------------- */
    static void adminPanel() throws IOException {
        boolean inAdmin = true;
        while (inAdmin) {
            System.out.println("\n----- ADMIN PANEL -----");
            System.out.println("1) View total payments");
            System.out.println("2) View total booked flights/tickets");
            System.out.println("3) View total available flights");
            System.out.println("4) Cancel a flight");
            System.out.println("5) Update tax rate");
            System.out.println("6) Add a new hotel");
            System.out.println("7) Back to main menu");
            System.out.print("Enter choice: ");
            String choice = readLine();

            switch (choice) {
                case "1": {
                    double total = 0;
                    for (Ticket t : tickets) if (!t.cancelled) total += t.amountPaid;
                    System.out.println("Total payments received: Rs." + String.format("%.2f", total));
                    break;
                }
                case "2": {
                    long activeBookings = tickets.stream().filter(t -> !t.cancelled).count();
                    System.out.println("Total booked tickets (active): " + activeBookings);
                    System.out.println("Total tickets ever issued: " + tickets.size());
                    break;
                }
                case "3": {
                    long activeFlights = flights.stream().filter(f -> !f.cancelled && !cancelledFlightIds.contains(f.flightId)).count();
                    System.out.println("Total available flights: " + activeFlights + " / " + flights.size());
                    break;
                }
                case "4": {
                    System.out.print("Enter flight ID to cancel: ");
                    String fid = readLine();
                    boolean found = false;
                    for (Flight f : flights) {
                        if (f.flightId.equalsIgnoreCase(fid)) {
                            f.cancelled = true;
                            cancelledFlightIds.add(f.flightId);
                            found = true;
                            System.out.println("Flight " + fid + " cancelled. It will no longer show in user menu.");
                            DatabaseHelper.cancelFlight(fid);
                            break;
                        }
                    }
                    if (!found) System.out.println("Flight ID not found.");
                    break;
                }
                case "5": {
                    System.out.print("Enter new tax rate (e.g. 0.05 for 5%, 0 to remove tax): ");
                    try {
                        double rate;
                        rate = Double.parseDouble(readLine());
                        taxRate = rate;
                        System.out.println("Tax rate updated to " + (taxRate * 100) + "%");
                        DatabaseHelper.updateTax(taxRate);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid rate.");
                    }
                    break;
                }
                case "6": {
                    System.out.print("Enter city: ");
                    String city = readLine();
                    System.out.print("Enter hotel name: ");
                    String name = readLine();
                    System.out.print("Enter price per night: ");
                    double price;
                    try {
                        price = Double.parseDouble(readLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid price.");
                        break;
                    }
                    String id = "H" + (hotels.size() + 1);
                    Hotel h = new Hotel(id, name, city, price);
                    hotels.add(h);
                    System.out.println("Hotel added: " + h);
                    DatabaseHelper.addHotel(id, name, city, price);
                    break;
                }
                case "7":
                    inAdmin = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}

