package com.example.WebApp.AppUser;

import com.example.WebApp.Dto.UserDataRequest;
import com.example.WebApp.Email.AccountVerificationEmailContext;
import com.example.WebApp.Email.EmailService;
import com.example.WebApp.Email.ForgotPasswordEmailContext;
import com.example.WebApp.Exception.InvalidTokenException;
import com.example.WebApp.Exception.UnknownIdentifierException;
import com.example.WebApp.Exception.UserAlreadyExistException;
import com.example.WebApp.Security.SecureToken;
import com.example.WebApp.Security.SecureTokenRepositories;
import com.example.WebApp.Security.SecureTokenService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private AppUserRepositories userRepositories;

    private BCryptPasswordEncoder passwordEncoder;

    private SecureTokenService tokenService;

    private SecureTokenRepositories tokenRepositories;

    private EmailService emailService;

    private UserRoleRepositories roleRepositories;

    @Value("${site.base.url.https}")
    private String baseURL;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final AppUser customer = userRepositories.findByEmail(email);
        if (customer == null) {
            throw new UsernameNotFoundException(email);
        }
        boolean enabled = !customer.isAccountVerified(); // we can use this in case we want to activate account after customer verified the account
        UserDetails user = User
                .withUsername(customer.getEmail())
                .password(customer.getPassword())
                .disabled(enabled)
                .authorities("USER")
                .build();

        return user;
    }

    public void register(UserDataRequest request) throws UserAlreadyExistException {
        if (checkIfUserExist(request.getEmail())) {
            throw new UserAlreadyExistException("User already exist for this email.");
        }

        AppUser user = new AppUser();
        BeanUtils.copyProperties(request, user);
        encodePassword(request, user);
        updateCustomerRole(user);
        userRepositories.save(user);
        sendRegistrationConfirmationEmail(user);
    }

    private void updateCustomerRole(AppUser user){
        UserRole role = roleRepositories.findByCode("user");
        user.addUserRoles(role);
    }

    public boolean checkIfUserExist(String email) {
        return userRepositories.findByEmail(email) != null ? true : false;
    }

    public void sendRegistrationConfirmationEmail(AppUser user) {
        // create and set token for user
        SecureToken secureToken = tokenService.createSecureToken();
        secureToken.setUser(user);
        tokenRepositories.save(secureToken);

        // send email containing token to user
        AccountVerificationEmailContext emailContext = new AccountVerificationEmailContext();
        emailContext.init(user);
        emailContext.setToken(secureToken.getToken());
        emailContext.buildVerificationUrl(baseURL, secureToken.getToken());

        try {
            emailService.sendMail(emailContext);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyUser(String token) throws InvalidTokenException {
        // get token, get user, verify and remove token after verification
        SecureToken secureToken = tokenService.findByToken(token);
        if(Objects.isNull(secureToken) || !StringUtils.equals(token, secureToken.getToken()) || secureToken.isExpired()){
            throw new InvalidTokenException("Token is not valid");
        }
        AppUser user = userRepositories.getById(secureToken.getUser().getId());
        if(Objects.isNull(user)){
            return false;
        }
        user.setAccountVerified(true);
        userRepositories.save(user);

        // remove invalid password
        tokenService.removeToken(secureToken);
        return true;
    }

    public AppUser getUserById(String id) throws UnknownIdentifierException {
        AppUser user= userRepositories.findByEmail(id);
        if(user == null || BooleanUtils.isFalse(user.isAccountVerified())){
            // we will ignore in case account is not verified or account does not exist
            throw new UnknownIdentifierException("Unable to find account or account is not active");
        }
        return user;
    }

    private void encodePassword(UserDataRequest source, AppUser target) {
        target.setPassword(passwordEncoder.encode(source.getPassword()));
    }

    public void forgottenPassword(String userName) throws UnknownIdentifierException {
        AppUser user = getUserById(userName);
        sendResetPasswordEmail(user);
    }

    public void updatePassword(String password, String token) throws InvalidTokenException, UnknownIdentifierException {
        SecureToken secureToken = tokenService.findByToken(token);
        if(Objects.isNull(secureToken) || !StringUtils.equals(token, secureToken.getToken()) || secureToken.isExpired()){
            throw new InvalidTokenException("Token is not valid");
        }
        AppUser user = userRepositories.getById(secureToken.getUser().getId());
        if(Objects.isNull(user)){
            throw new UnknownIdentifierException("unable to find user for the token");
        }
        tokenService.removeToken(secureToken);
        user.setPassword(passwordEncoder.encode(password));
        userRepositories.save(user);
    }


    protected void sendResetPasswordEmail(AppUser user) {
        SecureToken secureToken = tokenService.createSecureToken();
        secureToken.setUser(user);
        tokenRepositories.save(secureToken);
        ForgotPasswordEmailContext emailContext = new ForgotPasswordEmailContext();
        emailContext.init(user);
        emailContext.setToken(secureToken.getToken());
        emailContext.buildVerificationUrl(baseURL, secureToken.getToken());
        try {
            emailService.sendMail(emailContext);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
