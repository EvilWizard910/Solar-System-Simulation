package com.example.planetsimdemo;

public record AuthSession(
        String uid,
        String email,
        String idToken,
        String refreshToken,
        long expiresInSeconds
){
    public boolean isAuthenticated(){
        return uid!=null && !uid.isBlank() && idToken!=null && !idToken.isBlank();
    }
}
