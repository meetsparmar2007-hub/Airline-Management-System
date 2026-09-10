package ds;

/**
 * DS PACKAGE - Data structure / model class for a demo/known bank card
 * (used only to look up premium / bank-specific discounts).
 */
public class BankCard {
    public String cardNumber;
    public String holderName;
    public String cvv;
    public boolean premium;
    public String bank;

    public BankCard(String cardNumber, String holderName, String cvv, boolean premium, String bank) {
        this.cardNumber = cardNumber;
        this.holderName = holderName;
        this.cvv = cvv;
        this.premium = premium;
        this.bank = bank;
    }
}

