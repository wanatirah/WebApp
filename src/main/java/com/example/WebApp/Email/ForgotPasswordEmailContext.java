package com.example.WebApp.Email;

import com.example.WebApp.AppUser.AppUser;
import org.springframework.web.util.UriComponentsBuilder;

public class ForgotPasswordEmailContext extends AbstractEmailContext {

    private String token;

    @Override
    public <T> void init(T context){
        AppUser customer = (AppUser) context; // we pass the customer information
        put("firstName", customer.getFirstName());
        setTemplateLocation("emails/forgot-password");
        setSubject("Forgotten Password");
        setFrom("atirahadnan95@gmail.com");
        setTo(customer.getEmail());
    }

    public void setToken(String token) {
        this.token = token;
        put("token", token);
    }

    public void buildVerificationUrl(final String baseURL, final String token){
        final String url = UriComponentsBuilder.fromHttpUrl(baseURL)
                .path("/password/change").queryParam("token", token).toUriString();
        put("verificationURL", url);
    }
}
