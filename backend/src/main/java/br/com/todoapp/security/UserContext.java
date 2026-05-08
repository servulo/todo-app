package br.com.todoapp.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
public class UserContext {

    @Inject
    JsonWebToken jwt;

    public String getUserId() {
        return jwt.getSubject();
    }
}
