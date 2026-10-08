package com.tight.coupling;

// Tüm kullanıcı bilgilerini yönetmekten sorumludur.
public class UserManager {

    private UserDatabase userDatabase = new UserDatabase();

    public String getUserInfo() {
        return userDatabase.getUserDetails();
    }
}
