public class Platinum extends Customer {
    private int bonusBucks;

    public Platinum(String firstName, String lastName, String guestID, float amountSpent, int bonusBucks) {
        super(firstName, lastName, guestID, amountSpent);
        this.bonusBucks = bonusBucks;
    }

    public int getBonusBucks() {
        return bonusBucks;
    }

    public void setBonusBucks(int bonusBucks) {
        this.bonusBucks = bonusBucks;
    }

    @Override
    public String toString() {
        return super.toString() + " Platinum " + bonusBucks;
    }
}
