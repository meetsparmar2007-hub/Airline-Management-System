package ds;
import java.util.HashSet;
import java.util.Set;

/**
 * DS PACKAGE - Data structure / model class.
 * Holds all details of a single flight, including seat bookings (HashSet).
 */
public class Flight{
    public String flightId;
    public String from;
    public String to;
    public String timeSlot;
    public boolean international;
    public double economyPrice;
    public double businessPrice;
    public int totalEconomySeats = 50;
    public int totalBusinessSeats = 10;
    public Set<String> bookedSeats = new HashSet<>();   // DS: HashSet
    public boolean cancelled = false;

    public Flight(String flightId, String from, String to, String timeSlot, boolean international, double economyPrice, double businessPrice) {
        this.flightId = flightId;
        this.from = from;
        this.to = to;
        this.timeSlot = timeSlot;
        this.international = international;
        this.economyPrice = economyPrice;
        this.businessPrice = businessPrice;
    }

    public int emptyEconomySeats() {
        int used = 0;
        for (String s : bookedSeats) {
            if (s.startsWith("E")) used++;
        }
        return totalEconomySeats - used;
    }

    public int emptyBusinessSeats() {
        int used = 0;
        for (String s : bookedSeats) {
            if (s.startsWith("B")) used++;
        }
        return totalBusinessSeats - used;
    }

    public String toString() {
        return flightId + " | " + from + " -> " + to + " | " + timeSlot + " | " + (international ? "International" : "Domestic") + " | Economy Rs." + economyPrice + " (seats left " + emptyEconomySeats() + ")" + " | Business Rs." + businessPrice + " (seats left " + emptyBusinessSeats() + ")";
    }
}


