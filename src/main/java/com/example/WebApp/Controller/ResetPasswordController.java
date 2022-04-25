package com.example.WebApp.Controller;

import com.example.WebApp.AppUser.AppUserService;
import com.example.WebApp.Dto.ResetPasswordRequest;
import com.example.WebApp.Exception.InvalidTokenException;
import com.example.WebApp.Exception.UnknownIdentifierException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/password")
public class ResetPasswordController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String MSG = "resetPasswordMsg";

    private MessageSource messageSource;

    private AppUserService userService;

    @PostMapping("/request")
    public String resetPassword(ResetPasswordRequest forgotPasswordForm, RedirectAttributes redirAttr) {
        try {
            userService.forgottenPassword(forgotPasswordForm.getEmail());
        } catch (UnknownIdentifierException e) {
            // log the error
        }

        redirAttr.addFlashAttribute(MSG, messageSource.getMessage("user.forgotpwd.msg", null, LocaleContextHolder.getLocale()));
        return REDIRECT_LOGIN;
    }

    @GetMapping("/change")
    public String changePassword(@RequestParam(required = false) String token, final RedirectAttributes redirAttr, final Model model) {
        if (StringUtils.isEmpty(token)) {
            redirAttr.addFlashAttribute("tokenError", messageSource.getMessage("user.registration.verification.missing.token", null, LocaleContextHolder.getLocale()));
            return REDIRECT_LOGIN;
        }

        ResetPasswordRequest data = new ResetPasswordRequest();
        data.setToken(token);
        setResetPasswordForm(model, data);
        return "/account/changePassword";
    }

    @PostMapping("/change")
    public String changePassword(final ResetPasswordRequest data, final Model model) {
        try {
            userService.updatePassword(data.getPassword(), data.getToken());
        } catch (InvalidTokenException | UnknownIdentifierException e) {
            // log error statement
            model.addAttribute("tokenError", messageSource.getMessage("user.registration.verification.invalid.token", null, LocaleContextHolder.getLocale()));
            return "/account/changePassword";
        }
        model.addAttribute("passwordUpdateMsg", messageSource.getMessage("user.password.updated.msg", null, LocaleContextHolder.getLocale()));
        setResetPasswordForm(model, new ResetPasswordRequest());
        return "/account/changePassword";
    }

    private void setResetPasswordForm(final Model model, ResetPasswordRequest data){
        model.addAttribute("forgotPassword",data);
    }
}
