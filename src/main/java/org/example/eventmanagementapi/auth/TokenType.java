package org.example.eventmanagementapi.auth;

public enum TokenType {
    BEARER;

    @Override
    public String toString() {
        return "Bearer";
    }
}
