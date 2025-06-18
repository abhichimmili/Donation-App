package com.example.donation;
public class Orphanage {
    private String orphanageName, regNumber,password , contactPerson, orphanageAddress, orphanagePhone, email, userType;

    public Orphanage() {}  // Needed for Firebase

    public Orphanage(String orphanageName, String regNumber,String password , String contactPerson, String orphanageAddress, String orphanagePhone, String email, String userType) {
        this.orphanageName = orphanageName;
        this.regNumber = regNumber;
        this.password = password;
        this.contactPerson = contactPerson;
        this.orphanageAddress = orphanageAddress;
        this.orphanagePhone = orphanagePhone;
        this.email = email;
        this.userType = userType;
    }

    // Add public getters (and setters if needed)
    public String getOrphanageName() { return orphanageName; }
    public String getRegNumber() { return regNumber; }
    public String getPassword(){return password;}
    public String getContactPerson() { return contactPerson; }
    public String getOrphanageAddress() { return orphanageAddress; }
    public String getOrphanagePhone() { return orphanagePhone; }
    public String getEmail() { return email; }
    public String getUserType() { return userType; }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOrphanageName(String orphanageName) {
        this.orphanageName = orphanageName;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public void setOrphanagePhone(String orphanagePhone) {
        this.orphanagePhone = orphanagePhone;
    }

    public void setOrphanageAddress(String orphanageAddress) {
        this.orphanageAddress = orphanageAddress;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
