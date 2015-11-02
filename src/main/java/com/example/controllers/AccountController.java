package com.example.controllers;

import com.example.Api;
import com.example.daos.UserRepo;
import com.example.models.User;
import com.example.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import static com.example.Api.*;

@RestController
public class AccountController {

    static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    UserRepo userRepo;

    @Autowired
    TokenService tokenService;

    /**
     * @param phone
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public @ResponseBody Api.Result register(@RequestParam String phone) {
        User u = userRepo.findOneByPhone(phone);
        if (u == null) {
            User user = new User(phone);
            userRepo.save(user);
            return Api.result(SUCCESS).param("token").value(tokenService.generateToken(user));
        }
        return Api.result(ERR_PHONE_EXISTED);
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView register() {
        return new ModelAndView("register");
    }
}
