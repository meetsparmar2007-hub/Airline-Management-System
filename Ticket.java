package ds;

/**
 * DS PACKAGE - Data structure / model class for an issued Ticket
 * (Flight / Hotel / Taxi).
 */
public class Ticket {
    public String ticketCode;
    public String type;          // FLIGHT / HOTEL / TAXI
    public String userMobile;
    public String userName;
    public String details;
    public double amountPaid;
    public String bookingTime;
    public boolean cancelled = false;

    public Ticket(String ticketCode, String type, String userMobile, String userName, String details, double amountPaid, String bookingTime) {
        this.ticketCode = ticketCode;
        this.type = type;
        this.userMobile = userMobile;
        this.userName = userName;
        this.details = details;
        this.amountPaid = amountPaid;
        this.bookingTime = bookingTime;
    }

    public void print() {
        System.out.println("\n===================== TICKET =====================");
        System.out.println("Ticket Code   : " + ticketCode);
        System.out.println("Type          : " + type);
        System.out.println("Passenger     : " + userName + " (" + userMobile + ")");
        System.out.println("Details       : " + details);
        System.out.println("Amount Paid   : Rs." + String.format("%.2f", amountPaid));
        System.out.println("Booking Time  : " + bookingTime);
        System.out.println("Status        : " + (cancelled ? "CANCELLED" : "CONFIRMED"));
        System.out.println("===================================================\n");
    }
}

