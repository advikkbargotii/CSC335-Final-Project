/**
  Description: The Expense class represents a single expense record with details such as
			  date, category, amount, and description.
			  This class encapsulates its data through private fields and provides 
			  public getter and setter methods for controlled access.
*/

import java.time.LocalDate;

public class Expense {
    private LocalDate date;  // The date of the expense
    private String category; // The category of the expense
    private double amount;  // The amount of the expense
    private String description; // A brief description of the expense

    
    /**
    Constructs an Expense object with the specified date, category, amount, and description.
    @param date The date of the expense.
    @param category The category of the expense (e.g., Food, Transport).
    @param amount The amount of the expense.
    @param description A brief description of the expense.
    */
    public Expense(LocalDate date, String category, double amount, String description) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    /**
    Gets the date of the expense.
    @return The date of the expense.
    */
    public LocalDate getDate() {
        return date;
    }

    /**
    Sets the date of the expense.
    @param date The new date for the expense.
    */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
    Gets the category of the expense.
    @return The category of the expense.
    */
    public String getCategory() {
        return category;
    }

    /**
    Sets the category of the expense.
    @param category The new category for the expense.
    */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
    Gets the amount of the expense.
    @return The amount of the expense.
    */
    public double getAmount() {
        return amount;
    }

    /**
    Sets the amount of the expense.
    @param amount The new amount for the expense.
    */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
    Gets the description of the expense.
    @return The description of the expense.
    */
    public String getDescription() {
        return description;
    }

    /**
    Sets the description of the expense.
    @param description The new description for the expense.
    */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
    Provides a string representation of the expense, including date, category, amount, and description.
    @return A string representation of the expense.
    */
    @Override
    public String toString() {
        return date + " - " + category + " - $" + amount + " - " + description;
    }
}
