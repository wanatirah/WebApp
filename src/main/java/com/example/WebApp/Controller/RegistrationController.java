package com.example.WebApp.Controller;

import com.example.WebApp.AppUser.UserService;
import com.example.WebApp.Dto.UserDataRequest;
import com.example.WebApp.Exception.InvalidTokenException;
import com.example.WebApp.Exception.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

    private static final String REDIRECT_LOGIN = "redirect:/login";

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String register(final Model model) {
        model.addAttribute("userData", new UserDataRequest());
        return "register";
    }

    @PostMapping
    public String userRegistration(final @Valid UserDataRequest userData, final BindingResult bindingResult, final Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registrationForm", userData);
            return "register";
        }

        try {
            userService.register(userData);
        } catch (UserAlreadyExistException e) {
            bindingResult.rejectValue("email", "userData.email", "An account already exists for this email.");
            model.addAttribute("registrationForm", userData);
            return "register";
        }

        model.addAttribute("registrationMsg", messageSource.getMessage("user.registration.verification.email.msg", null, LocaleContextHolder.getLocale()));
        return "register";
    }

    @GetMapping("/verify")
    public String verifyCustomer(@RequestParam(required = false) String token, final Model model, RedirectAttributes redirAttr) {
        if (StringUtils.isEmpty(token)) {
            redirAttr.addFlashAttribute("tokenError", messageSource.getMessage("user.registration.verification.missing.token", null, LocaleContextHolder.getLocale()));
            return REDIRECT_LOGIN;
        }

        try {
            userService.verifyUser(token);
        } catch (InvalidTokenException e) {
            redirAttr.addFlashAttribute("tokenError", messageSource.getMessage("user.registration.verification.invalid.token", null, LocaleContextHolder.getLocale()));
            return REDIRECT_LOGIN;
        }

        redirAttr.addFlashAttribute("verifiedAccountMsg", messageSource.getMessage("user.registration.verification.success", null, LocaleContextHolder.getLocale()));
        return REDIRECT_LOGIN;
    }
}
