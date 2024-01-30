public class Gold extends Customer {
    private int discount; // in percentage

    public Gold(String firstName, String lastName, String guestID, float amountSpent, int discount) {
        super(firstName, lastName, guestID, amountSpent);
        this.discount = discount;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return super.toString() + " Gold " + discount;
    }
}