package org.example.dummybank.Model;

public class BankTopupDTO {
    private String phoneNo;
    private int amount;
    private String jwtToken;
    public String getPhoneNo() {
        return phoneNo;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

}
