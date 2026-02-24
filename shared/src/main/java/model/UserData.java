package model;

public record UserData (String username, String password, String email){
    public UserData {
        if (username == null || password == null){
            throw new IllegalArgumentException("Username and password cannot be null");
        }
    }
}
