package org.example.dummybank.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class BankUser {
    @Id
    private String mobileNumber;
    private int balance;
}
