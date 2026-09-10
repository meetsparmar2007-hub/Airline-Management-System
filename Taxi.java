package ds;

/**
 * DS PACKAGE - Data structure / model class for a Taxi.
 */
public class Taxi {
    public String taxiId;
    public String name;
    public String city;
    public String mobile;
    public double perKmPrice = 10.0;

    public Taxi(String taxiId, String name, String city, String mobile) {
        this.taxiId = taxiId;
        this.name = name;
        this.city = city;
        this.mobile = mobile;
    }

    public String toString() {
        return taxiId + " | Driver: " + name + " | Mobile: " + mobile + " | Rs." + perKmPrice + "/km";
    }
}

