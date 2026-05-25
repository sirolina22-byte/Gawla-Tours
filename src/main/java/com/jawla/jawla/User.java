package com.jawla.jawla;

public class User {
    private String fullName;
    private String email;
    private String nationalId; // الحقل ده لازم يكون موجود
    private String role;

    // الباني الفاضي اللي عملناه سوا
    public User() {}

    // الباني اللي بياخد بيانات (لو محتاجاه)
    public User(String fullName, String email, String role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // الـ Getters والـ Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // دي الميثود اللي ناقصة وعاملة إيرور في الساين أب:
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}