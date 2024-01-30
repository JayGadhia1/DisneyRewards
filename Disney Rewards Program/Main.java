import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        ensureFileExists("customer.dat");
        ensureFileExists("preferred.dat");

        System.out.println("Enter the regular customer file: ");
        String regularCustomerFile = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        System.out.println("Enter the preferred customer file: ");
        String preferredCustomerFile = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        System.out.println("Enter the orders file: ");
        String ordersFile = scanner.hasNextLine() ? scanner.nextLine().trim() : "";

        Customer[] regularCustomers = new Customer[10];
        Customer[] preferredCustomers = new Customer[10];

        readCustomerFile(regularCustomerFile, regularCustomers);
        readPreferredCustomerFile(preferredCustomerFile, preferredCustomers);

        processOrders(ordersFile, regularCustomers, preferredCustomers);

        //writeCustomerToFile("customer.dat", regularCustomers);

       //writePreferredCustomerToFile("preferred.dat", preferredCustomers);
    }

    public static void sortPreferredCustomers(Customer[] preferredCustomers) {
        Arrays.sort(preferredCustomers, new Comparator<Customer>() {
            @Override
            public int compare(Customer c1, Customer c2) {
                if (c1 == null && c2 == null) return 0;
                if (c1 == null) return 1;
                if (c2 == null) return -1;
                // First compare by amount spent (in ascending order)
                int amountSpentComparison = Float.compare(c1.getAmountSpent(), c2.getAmountSpent());
                if (amountSpentComparison != 0) return amountSpentComparison;
                // Then compare by guest ID (in ascending order)
                return c1.getGuestID().compareTo(c2.getGuestID());
            }
        });
    }

    public static void readCustomerFile(String fileName, Customer[] array) {
        try (Scanner fileScanner = new Scanner(new File(fileName))) {
            int count = 0;
            while (fileScanner.hasNextLine() && count < array.length) {
                String[] data = fileScanner.nextLine().split(" ");
                array[count] = new Customer(data[1], data[2], data[0], Float.parseFloat(data[3]));
                count++;
            }
        } catch (IOException e) {
            System.out.println("Error reading customer file: " + e.getMessage());
        }
    }    


    public static void readPreferredCustomerFile(String fileName, Customer[] array) {
        try (Scanner fileScanner = new Scanner(new File(fileName))) {
            int count = 0;
            while (fileScanner.hasNextLine() && count < array.length) {
                String[] data = fileScanner.nextLine().split("\\s+");
                float amountSpent = Float.parseFloat(data[3]);
                int discount = Math.round(Float.parseFloat(data[4].replace("%", "")));
                array[count] = new Gold(data[1], data[2], data[0], amountSpent, discount);
                count++;
            }
        } catch (IOException e) {
            System.out.println("Error reading preferred customer file: " + e.getMessage());
        }
    }

    public static void processOrders(String fileName, Customer[] regularCustomers, Customer[] preferredCustomers) {
        try (Scanner orderScanner = new Scanner(new File(fileName))) {
            while (orderScanner.hasNextLine()) {
                String line = orderScanner.nextLine();
                String[] parts = line.split(" ");
                if (parts.length < 5) {
                    System.out.println("Skipping invalid order line: " + line);
                    continue;  // skip to the next iteration of the loop
                }
    
                String guestID = parts[0];
                String size = parts[1];
                String drinkType = parts[2];
                float customPricePerOunce;
                try {
                    customPricePerOunce = Float.parseFloat(parts[3]);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid order line (invalid custom price): " + line);
                    continue;  // skip to the next iteration of the loop
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(parts[4]);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid order line (invalid quantity): " + line);
                    continue;  // skip to the next iteration of the loop
                }
    
                float drinkPricePerOunce = 0;
    
                switch (drinkType.toLowerCase()) {
                    case "soda":
                        drinkPricePerOunce = 0.20f;
                        break;
                    case "tea":
                        drinkPricePerOunce = 0.12f;
                        break;
                    case "punch":
                        drinkPricePerOunce = 0.15f;
                        break;
                    default:
                        System.out.println("Invalid Drink: " + drinkType);
                        continue;
                }
    
                float drinkVolume = 0;
                float surfaceArea = 0;
    
                switch (size) {
                    case "S":
                        drinkVolume = 12;
                        surfaceArea = (float)(2 * Math.PI * 2 * 4.5);
                        break;
                    case "M":
                        drinkVolume = 20;
                        surfaceArea = (float)(2 * Math.PI * 2.25 * 5.75);
                        break;
                    case "L":
                        drinkVolume = 32;
                        surfaceArea = (float)(2 * Math.PI * 2.75 * 7);
                        break;
                }
    
                float orderAmount = (drinkVolume * drinkPricePerOunce + surfaceArea * customPricePerOunce) * quantity;
    
                
                boolean foundInPreferred = false;
                for (int i = 0; i < preferredCustomers.length && !foundInPreferred; i++) {
                    if (preferredCustomers[i] != null && preferredCustomers[i].getGuestID().equals(guestID)) {
                        foundInPreferred = true;
            
                if (preferredCustomers[i] instanceof Gold) {
                Gold gold = (Gold) preferredCustomers[i];
                float discount = 1 - gold.getDiscount() / 100f;
                float updatedAmount = gold.getAmountSpent() + orderAmount * discount;
                gold.setAmountSpent(updatedAmount);

                if (updatedAmount >= 150) {
                    gold.setDiscount(15);
                } else if (updatedAmount >= 100) {
                    gold.setDiscount(10);
                } else {
                    gold.setDiscount(5);
                }

                if (updatedAmount >= 200) {
                    int bonusBucks = (int) ((updatedAmount - 200) / 5);
                    Customer platinum = new Platinum(gold.getFirstName(), gold.getLastName(), guestID, updatedAmount, bonusBucks);
                    preferredCustomers[i] = platinum;
                }
                } else if (preferredCustomers[i] instanceof Platinum) {
                    Platinum platinum = (Platinum) preferredCustomers[i];
                    int bonusBucks = platinum.getBonusBucks();
                
                    // Determine how many bonus bucks to use for the order.
                    int bonusBucksUsed = Math.min(bonusBucks, (int) orderAmount);
                    bonusBucks -= bonusBucksUsed;
                
                    float adjustedOrderAmount = orderAmount - bonusBucksUsed;
                
                    // Adjust total spent without considering the bonus bucks earned from this order.
                    float updatedAmount = platinum.getAmountSpent() + adjustedOrderAmount;
                
                    // Calculate bonus bucks earned from the current order.
                    if (updatedAmount > 200) {
                        bonusBucks += (int) ((updatedAmount - 200) / 5);
                    }
                    
                    platinum.setAmountSpent(updatedAmount);
                    platinum.setBonusBucks(bonusBucks);
                }
            
                }
                } 
                if (!foundInPreferred) {
                    for (int i = 0; i < regularCustomers.length; i++) {
                        if (regularCustomers[i] != null && regularCustomers[i].getGuestID().equals(guestID)) {
                            float updatedAmount = regularCustomers[i].getAmountSpent() + orderAmount;
                            if (updatedAmount >= 50 && updatedAmount < 100) {
                                float discountAmount = orderAmount * 0.95f;
                                updatedAmount = regularCustomers[i].getAmountSpent() + discountAmount;
                                Customer newGoldCustomer = new Gold(regularCustomers[i].getFirstName(), regularCustomers[i].getLastName(), guestID, updatedAmount, 5);
                                addToPreferred(preferredCustomers, newGoldCustomer);
                                removeFromRegular(regularCustomers, i);
                            } else if (updatedAmount >= 100 && updatedAmount < 150) {
                                float discountAmount = orderAmount * 0.90f;
                                updatedAmount = regularCustomers[i].getAmountSpent() + discountAmount;
                                Customer newGoldCustomer = new Gold(regularCustomers[i].getFirstName(), regularCustomers[i].getLastName(), guestID, updatedAmount, 10);
                                addToPreferred(preferredCustomers, newGoldCustomer);
                                removeFromRegular(regularCustomers, i);
                            } else if (updatedAmount >= 150) {
                                float discountAmount = orderAmount * 0.85f;  // Apply 15% discount
                                updatedAmount = regularCustomers[i].getAmountSpent() + discountAmount;
                                Customer newGoldCustomer = new Gold(regularCustomers[i].getFirstName(), regularCustomers[i].getLastName(), guestID, updatedAmount, 15);
                                addToPreferred(preferredCustomers, newGoldCustomer);
                                removeFromRegular(regularCustomers, i);
                            } else {
                                regularCustomers[i].setAmountSpent(updatedAmount);
                            }
                            
                            break;
                        }
                    }
                    
                }
            }
            writeCustomerToFile("customer.dat", regularCustomers);
            writePreferredCustomerToFile("preferred.dat", preferredCustomers);
            
        } catch (IOException e) {
            System.out.println("Error reading order file: " + e.getMessage());
        }
    }
    
    
    public static void removeFromRegular(Customer[] regularCustomers, int index) {
        regularCustomers[index] = null;
    }

    public static void addToPreferred(Customer[] preferredCustomers, Customer customer) {
        for (int i = 0; i < preferredCustomers.length; i++) {
            if (preferredCustomers[i] == null) {
                preferredCustomers[i] = customer;
                System.out.println("Added to preferred: " + customer); // Debugging line
                break;
            }
        }
    }       

    private static void ensureFileExists(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating " + fileName + ": " + e.getMessage());
            }
        }
    }    

    public static void writeCustomerToFile(String fileName, Customer[] array) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (Customer c : array) {
                if (c != null) {
                    writer.println(c.getGuestID() + " " + c.getFirstName() + " " + c.getLastName() + " " + String.format("%.2f", c.getAmountSpent()));
                }
            }
        } catch (IOException e) {
            System.out.println("Error writing to " + fileName + ": " + e.getMessage());
        }
    }
    
    public static void writePreferredCustomerToFile(String fileName, Customer[] array) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (Customer pc : array) {
                if (pc != null) {
                    if (pc instanceof Gold) {
                        if (pc.getAmountSpent() >= 200) {
                            writer.println(pc.getGuestID() + " " + pc.getFirstName() + " " + pc.getLastName() + " " + String.format("%.2f", pc.getAmountSpent()) + " " + ((Gold) pc).getDiscount());
                        } else {
                            writer.println(pc.getGuestID() + " " + pc.getFirstName() + " " + pc.getLastName() + " " + String.format("%.2f", pc.getAmountSpent()) + " " + ((Gold) pc).getDiscount() + "%");
                        }
                    } else if (pc instanceof Platinum) {
                        writer.println(pc.getGuestID() + " " + pc.getFirstName() + " " + pc.getLastName() + " " + String.format("%.2f", pc.getAmountSpent()) + " " + ((Platinum) pc).getBonusBucks());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error writing preferred customer file: " + e.getMessage());
        }
    }
    
    
}
