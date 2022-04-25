package com.example.WebApp.Controller;

import com.example.WebApp.AppUser.AppUserService;
import com.example.WebApp.Dto.UserDataRequest;
import com.example.WebApp.Exception.InvalidTokenException;
import com.example.WebApp.Exception.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
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
import org.thymeleaf.util.StringUtils;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegistrationController {

    private AppUserService userService;

    private MessageSource messageSource;

    @GetMapping("/register")
    public String register(final Model model) {
        model.addAttribute("userData", new UserDataRequest());
        return "account/register";
    }

    // after the validation the information is posted
    @PostMapping
    public String userRegistration(final @Valid UserDataRequest userData, final BindingResult bindingResult, final Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userData", userData);
            return "pages-register";
        }

        try {
            userService.register(userData);
        } catch (UserAlreadyExistException e) {
            bindingResult.rejectValue("email", "userData.email", "An account already exists for this email.");
            model.addAttribute("registrationForm", userData);
            return "pages-register";
        }

        return "redirect:account/starter";
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam(required = false) String token, final Model model, RedirectAttributes redirectAttributes) {
        if (StringUtils.isEmpty(token)) {
            redirectAttributes.addFlashAttribute("tokenError", messageSource.getMessage("user.registration.verification.missing.token", null, LocaleContextHolder.getLocale()));
            return "redirect:/pages-login";
        }
        try {
            userService.verifyUser(token);
        } catch (InvalidTokenException e) {
            redirectAttributes.addFlashAttribute("tokenError", messageSource.getMessage("user.registration.verification.invalid.token", null,LocaleContextHolder.getLocale()));
            return "redirect:/pages-login";
        }

        redirectAttributes.addFlashAttribute("verifiedAccountMsg", messageSource.getMessage("user.registration.verification.success", null,LocaleContextHolder.getLocale()));
        return "redirect:/pages-login";
    }
}
