public class Customer {
    private String firstName;
    private String lastName;
    private String guestID;
    private float amountSpent;

    public Customer(String firstName, String lastName, String guestID, float amountSpent) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.guestID = guestID;
        this.amountSpent = amountSpent;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGuestID() {
        return guestID;
    }

    public float getAmountSpent() {
        return amountSpent;
    }

    public void setAmountSpent(float amountSpent) {
        this.amountSpent = amountSpent;
    }

    @Override
    public String toString() {
        return guestID + " " + firstName + " " + lastName + " " + String.format("%.2f", amountSpent);
}

}
