package com.chisimdi.user.service.controllers;

import com.chisimdi.user.service.models.User;
import com.chisimdi.user.service.models.UserDTO;
import com.chisimdi.user.service.services.UserService;
import com.chisimdi.user.service.utils.LoginRequest;
import com.chisimdi.user.service.utils.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController {
    private UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }

    @Operation(summary = "Registers a user")
    @PostMapping("/register")
    public UserDTO register(@RequestBody @Valid User user){
        return userService.addUser(user);
    }

    @Operation(summary = "Retrieves all customers, available only to admins")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/customers")
    public List<UserDTO> findAllCustomers(@RequestParam(defaultValue = "0")int pageNumber, @RequestParam(defaultValue = "10")int size){
        return userService.findAllCustomers(pageNumber, size);
    }

    @Operation(summary = "Retrieves all merchants, available only to admins")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/merchants")
    public List<UserDTO> findAllMerchants(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return userService.findAllMerchants(pageNumber, size);
    }

    @Operation(summary = "Retrieves all users, available only to admins")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/")
    public List<UserDTO> findAllUsers(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return userService.findAllUsers(pageNumber, size);
    }

    @Operation(summary = "Verifies if a user exists, available only to services")
    @PreAuthorize("hasRole('ROLE_Service')")
    @GetMapping("/{userId}/exists")
    public Boolean doesUserExist(@PathVariable("userId")int userId){
        return userService.doesUserExist(userId);
    }


    @Operation(summary = "Logs a user into their account")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest){
        return userService.login(loginRequest.getUserName(), loginRequest.getPassword());
    }
}
