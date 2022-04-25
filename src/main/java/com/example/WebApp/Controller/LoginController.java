package com.example.WebApp.Controller;

import com.example.WebApp.Dto.ResetPasswordRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/login")
public class LoginController {

    public static final String LAST_USERNAME_KEY = "LAST_USERNAME";

    @GetMapping
    public String login(@RequestParam(value = "error",defaultValue = "false") boolean loginError, final Model model, HttpSession session){
        String userName = getUserName(session);
        if(loginError){
            if(StringUtils.isNotEmpty(userName)){
                model.addAttribute("accountLocked", Boolean.TRUE);
                model.addAttribute("forgotPassword", new ResetPasswordRequest());
                return "account/login";
            }
        }

        model.addAttribute("forgotPassword", new ResetPasswordRequest());
        model.addAttribute("accountLocked", Boolean.FALSE);
        return "account/login";
    }

    final String getUserName(HttpSession session){
        final String username = (String) session.getAttribute(LAST_USERNAME_KEY);
        if(StringUtils.isNotEmpty(username)){
            session.removeAttribute(LAST_USERNAME_KEY); // we don't need it and removing it.
        }
        return username;
    }
}
