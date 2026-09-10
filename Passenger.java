package ds;

/**
 * DS PACKAGE - Data structure / model class for a Passenger.
 */
public class Passenger {
    public String name;
    public int age;
    public String gender;

    public String seatNumber;
    public String seatPosition;

    public Passenger(String name, int age, String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;

        this.seatNumber = "";
        this.seatPosition = "";
    }
}

