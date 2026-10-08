package com.loose.coupling;

// UserDatabase veritabanına erişmek için kullanılan bir sınıftır.
public class UserDatabaseProvider implements  UserDataProvider {
    public  String getUserDetails() {
        return "User Details from the database";
    }
}