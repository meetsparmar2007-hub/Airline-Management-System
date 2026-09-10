package ds;

/**
 * DS PACKAGE - Data structure / model class for a Hotel.
 */
public class Hotel {
    public String hotelId;
    public String name;
    public String city;
    public double pricePerNight;

    public Hotel(String hotelId, String name, String city, double pricePerNight) {
        this.hotelId = hotelId;
        this.name = name;
        this.city = city;
        this.pricePerNight = pricePerNight;
    }

    public String toString() {
        return hotelId + " | " + name + " | " + city + " | Rs." + pricePerNight + " per night";
    }
}
