package DBMS;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class DatabaseHelper {

    public static final String URL = "jdbc:mysql://localhost:3306/airline_db";
    public static final String USER = "root";
    public static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Save User
    public static void saveUser(String name, String mobile, String email) {
        try {
            Connection con = getConnection();

            CallableStatement cs = con.prepareCall("{call saveUser(?,?,?)}");

            cs.setString(1, name);
            cs.setString(2, mobile);
            cs.setString(3, email);

            cs.execute();

            System.out.println("[DB] User saved successfully.");

            cs.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Save Bank Card
    public static void saveBankCard(String cardNumber, String holderName, String cvv, String bank, boolean premium) {
        try {
            Connection con = getConnection();

            CallableStatement cst = con.prepareCall("{call saveBankCard(?,?,?,?,?)}");

            cst.setString(1, cardNumber);
            cst.setString(2, holderName);
            cst.setString(3, cvv);
            cst.setString(4, bank);
            cst.setBoolean(5, premium);

            cst.execute();

            System.out.println("[DB] Bank Card saved successfully.");

            cst.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Save Payment
    public static void savePayment(String userMobile, String userName, String cardLast4, String bank, boolean premium, double originalAmount, double discountPercent, double finalAmount, String paymentTime) {

        try {
            Connection con = getConnection();

            CallableStatement cs = con.prepareCall("{call savePayment(?,?,?,?,?,?,?,?,?)}");

            cs.setString(1, userMobile);
            cs.setString(2, userName);
            cs.setString(3, cardLast4);
            cs.setString(4, bank);
            cs.setBoolean(5, premium);
            cs.setDouble(6, originalAmount);
            cs.setDouble(7, discountPercent);
            cs.setDouble(8, finalAmount);
            cs.setString(9, paymentTime);

            cs.execute();

            cs.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Save Ticket

    public static void saveTicket(String ticketCode, String type, String userMobile, String userName, String passengerName, int age, String gender, String details, double amount, String bookingTime, String ticketFile) {

        String sql = "INSERT INTO tickets " + "(ticket_code, type, user_mobile, user_name, " + "passenger_name, passenger_age, passenger_gender, " + "details, amount_paid, booking_time, ticket_file) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {

            Connection con = getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, ticketCode);
            ps.setString(2, type);
            ps.setString(3, userMobile);
            ps.setString(4, userName);
            ps.setString(5, passengerName);
            ps.setInt(6, age);
            ps.setString(7, gender);
            ps.setString(8, details);
            ps.setDouble(9, amount);
            ps.setString(10, bookingTime);
            ps.setString(11, ticketFile);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("[DB] Ticket saved successfully.");
            } else {
                System.out.println("[DB] Ticket was NOT saved.");
            }

            ps.close();
            con.close();

        } catch (SQLException e) {

            System.out.println("Ticket Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // View logged-in user's total transactions
    public static void viewMyTransactions(String userMobile) {

        String sql = "SELECT * FROM payments " +"WHERE user_mobile = ? " +"ORDER BY payment_time DESC";
        try {
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, userMobile);

            ResultSet rs = ps.executeQuery();

            double totalAmount = 0;
            int transactionCount = 0;
            System.out.println("       MY TRANSACTIONS");
            while (rs.next()) {
                transactionCount++;
                String userName = rs.getString("user_name");
                String card = rs.getString("card");
                String bank = rs.getString("bank");
                boolean premium = rs.getBoolean("premium");
                double originalAmount = rs.getDouble("original_amount");
                double discount = rs.getDouble("discount_percent");
                double finalAmount = rs.getDouble("final_amount");
                String paymentTime = rs.getString("payment_time");
                totalAmount += finalAmount;
                System.out.println("Transaction " + transactionCount);
                System.out.println("User Name        : " + userName);
                System.out.println("Card             : " + card);
                System.out.println("Bank             : " + bank);
                System.out.println("Premium Card     : " + (premium ? "YES" : "NO"));
                System.out.println("Original Amount  : Rs. " + originalAmount);
                System.out.println("Discount         : " + discount + "%");
                System.out.println("Amount Paid      : Rs. " + finalAmount);
                System.out.println("Payment Time     : " + paymentTime);
            }
            if (transactionCount == 0) {
                System.out.println("No transactions found.");
            } else {
                System.out.println("Total Transactions : " + transactionCount);
                System.out.printf("Total Amount Paid  : Rs. %.2f%n", totalAmount);
            }
          } catch (SQLException e) {
            System.out.println("Transaction Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cancel Ticket
    public static void cancelTicket(String ticketCode) {

        try {
            Connection con = getConnection();

            CallableStatement cs = con.prepareCall("{call cancelTicket(?)}");

            cs.setString(1, ticketCode);

            cs.execute();

            cs.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Delete Booked Seat
    public static void deleteBookedSeat(String ticketCode) {

        try {
            Connection con = getConnection();

            CallableStatement cs = con.prepareCall("{call deleteBookedSeat(?)}");

            cs.setString(1, ticketCode);

            cs.execute();

            cs.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }// Cancel Flight

    public static void cancelFlight(String flightId) {

        try {
            Connection con = getConnection();

            CallableStatement cs = con.prepareCall("{call cancelFlight(?)}");

            cs.setString(1, flightId);

            cs.execute();

            cs.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }// Update Tax

    public static void updateTax(double taxRate) {

        try {
            Connection con = getConnection();

            CallableStatement cs = con.prepareCall("{call updateTax(?)}");

            cs.setDouble(1, taxRate);

            cs.execute();

            cs.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }// Add Hotel

    public static void addHotel(String id, String name, String city, double price) {

        try {
            Connection con = getConnection();

            CallableStatement cs = con.prepareCall("{call addHotel(?,?,?,?)}");

            cs.setString(1, id);
            cs.setString(2, name);
            cs.setString(3, city);
            cs.setDouble(4, price);

            cs.execute();

            cs.close();
            con.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
