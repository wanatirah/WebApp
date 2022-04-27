package com.example.WebApp.AppUser;

import com.example.WebApp.Dto.UserDataRequest;
import com.example.WebApp.Email.AccountVerificationEmailContext;
import com.example.WebApp.Email.EmailService;
import com.example.WebApp.Exception.InvalidTokenException;
import com.example.WebApp.Exception.UnknownIdentifierException;
import com.example.WebApp.Exception.UserAlreadyExistException;
import com.example.WebApp.Security.SecureToken;
import com.example.WebApp.Security.SecureTokenRepository;
import com.example.WebApp.Security.SecureTokenService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SecureTokenService secureTokenService;

    @Autowired
    private SecureTokenRepository secureTokenRepository;

    @Value("${site.base.url.https}")
    private String baseURL;

    public void register(UserDataRequest request) throws UserAlreadyExistException {
        if (checkIfUserExist(request.getEmail())) {
            throw new UserAlreadyExistException("User already exist for this email.");
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        encodePassword(request, user);
        userRepository.save(user);
        sendRegistrationConfirmationEmail(user);
    }

    public boolean checkIfUserExist(String email) {
        return userRepository.findByEmail(email) != null ? true : false;
    }

    public void sendRegistrationConfirmationEmail(User user) {
        // create and set token for user
        SecureToken secureToken = secureTokenService.createSecureToken();
        secureToken.setUser(user);
        secureTokenRepository.save(secureToken);

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
        SecureToken secureToken = secureTokenService.findByToken(token);
        if(Objects.isNull(secureToken) || !StringUtils.equals(token, secureToken.getToken()) || secureToken.isExpired()){
            throw new InvalidTokenException("Token is not valid");
        }
        User user = userRepository.getOne(secureToken.getUser().getId());
        if(Objects.isNull(user)){
            return false;
        }
        user.setAccountVerified(true);
        userRepository.save(user);

        // remove invalid password
        secureTokenService.removeToken(secureToken);
        return true;
    }

    public User getUserById(String id) throws UnknownIdentifierException {
        User user= userRepository.findByEmail(id);
        if(user == null || BooleanUtils.isFalse(user.isAccountVerified())){
            // we will ignore in case account is not verified or account does not exist
            throw new UnknownIdentifierException("Unable to find account or account is not active");
        }
        return user;
    }

    private void encodePassword(UserDataRequest source, User target) {
        target.setPassword(passwordEncoder.encode(source.getPassword()));
    }
}
