package com.chisimdi.user.service.services;

import com.chisimdi.user.service.exceptions.InvalidCredentialsException;
import com.chisimdi.user.service.mappers.UserMapper;
import com.chisimdi.user.service.models.User;
import com.chisimdi.user.service.models.UserDTO;
import com.chisimdi.user.service.repositories.UserRepository;
import com.chisimdi.user.service.utils.LoginResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private UserRepository userRepository;
    private UserMapper userMapper;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private JwtUtilService jwtUtilService;

    public UserService(UserRepository userRepository,UserMapper userMapper,BCryptPasswordEncoder bCryptPasswordEncoder,JwtUtilService jwtUtilService){
        this.userRepository=userRepository;
        this.userMapper=userMapper;
        this.bCryptPasswordEncoder=bCryptPasswordEncoder;
        this.jwtUtilService=jwtUtilService;
    }

    public UserDTO addUser(User user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return userMapper.toUserDTO(user);
    }

    public List<UserDTO> findAllUsers(int pageNumber, int size){
        Page<User>users=userRepository.findAll(PageRequest.of(pageNumber, size));
        return userMapper.toUserDTOList(users.getContent());
    }

    public List<UserDTO> findAllMerchants(int pageNumber, int size){
        Page<User>users=userRepository.findByRoles("Merchant",PageRequest.of(pageNumber, size));
        return userMapper.toUserDTOList(users.getContent());
    }

    public List<UserDTO> findAllCustomers(int pageNumber, int size){
        Page<User>users=userRepository.findByRoles("Customer",PageRequest.of(pageNumber, size));
        return userMapper.toUserDTOList(users.getContent());
    }
    public Boolean doesUserExist(int userId){
        return userRepository.existsById(userId);
    }
    public LoginResponse login(String userName, String password){
        User user= userRepository.findByUserName(userName);
        if(user==null){
            throw new InvalidCredentialsException("User name is not valid");
        }
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())){
            throw new InvalidCredentialsException("Password invalid");
        }

        String token =jwtUtilService.generateToken(user.getUserName(), user.getId(), user.getRoles());
        LoginResponse loginResponse=new LoginResponse();
        loginResponse.setUserName(jwtUtilService.extractUserName(token));
        loginResponse.setUserId(jwtUtilService.extractUserId(token));
        loginResponse.setRole(jwtUtilService.extractRole(token));
        loginResponse.setToken(token);
        return loginResponse;
    }
}
