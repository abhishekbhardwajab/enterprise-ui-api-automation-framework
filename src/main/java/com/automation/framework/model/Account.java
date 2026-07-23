package com.automation.framework.model;

import com.automation.framework.utils.TestDataGenerator;

import java.util.Objects;

/**
 * Plain domain object representing an automationexercise.com account: the
 * data passed into the signup pages, or reconstructed from a CSV record
 * loaded by UserRepository.
 *
 * Public fields by design - this is a transferable data holder, not a
 * behavior-bearing object, matching the rest of the model layer.
 */
public class Account {

    public Title title;
    public String firstName;
    public String lastName;
    public String email;
    public String password;
    public String address;
    public String state;
    public String city;
    public String zipcode;
    public String mobileNumber;

    public Account() {
    }

    public Account(Title title, String firstName, String lastName, String email, String password,
                    String address, String state, String city, String zipcode, String mobileNumber) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.state = state;
        this.city = city;
        this.zipcode = zipcode;
        this.mobileNumber = mobileNumber;
    }

    /**
     * Builds a disposable account with a unique email so the signup flow
     * never collides with a previously-created account, regardless of how
     * many times or how many parallel threads run it.
     */
    public static Account random() {
        return new Account(
                Title.MR,
                "QE",
                "Automation",
                TestDataGenerator.uniqueEmail(),
                "TestPass123!",
                "123 Automation Way",
                "Ontario",
                "Toronto",
                TestDataGenerator.randomZipcode(),
                TestDataGenerator.randomMobileNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return Objects.equals(email, account.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Account{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
