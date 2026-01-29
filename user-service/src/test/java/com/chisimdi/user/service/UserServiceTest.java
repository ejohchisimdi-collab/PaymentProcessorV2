package com.chisimdi.user.service;

import com.chisimdi.user.service.exceptions.InvalidCredentialsException;
import com.chisimdi.user.service.mappers.UserMapper;
import com.chisimdi.user.service.models.User;
import com.chisimdi.user.service.repositories.UserRepository;
import com.chisimdi.user.service.services.JwtUtilService;
import com.chisimdi.user.service.services.UserService;
import com.chisimdi.user.service.utils.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.InvalidClassException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private JwtUtilService jwtUtilService;
    @InjectMocks
    UserService userService;

    @Test
    void registerTest(){
        User user=new User();
        user.setPassword("1000");

        when(bCryptPasswordEncoder.encode(user.getPassword())).thenReturn("9");
        when(userRepository.save(user)).thenReturn(user);

        userService.addUser(user);

        assertThat(user.getPassword()).isEqualTo("9");

        verify(userRepository).save(user);
        verify(bCryptPasswordEncoder).encode("1000");
    }

    @Test
    void loginTest_HappyPath(){
        String username="Chisimdi";
        String password="Ejoh";
        User user=new User();
        user.setId(1);
        user.setRoles("Admin");
        user.setUserName("Chisimdi");
        user.setPassword("1000");

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(password,user.getPassword())).thenReturn(true);
        when(jwtUtilService.generateToken(user.getUserName(),user.getId(),user.getRoles())).thenReturn("abc");

        LoginResponse loginResponse=userService.login(username,password);

        assertThat(loginResponse.getToken()).isEqualTo("abc");

        verify(userRepository).findByUserName(username);
        verify(bCryptPasswordEncoder).matches(password,user.getPassword());
        verify(jwtUtilService).generateToken(user.getUserName(),user.getId(),user.getRoles());

    }

    @Test
    void loginTest_ThrowsInvalidCredentialsExceptionForUserName(){
        String username="Chisimdi";
        String password="Ejoh";
        User user=new User();
        user.setId(1);
        user.setRoles("Admin");
        user.setUserName("Chisimdi");
        user.setPassword("1000");

        when(userRepository.findByUserName(username)).thenReturn(null);


        assertThatThrownBy(()->userService.login(username,password)).isInstanceOf(InvalidCredentialsException.class);



        verify(userRepository).findByUserName(username);
        verify(bCryptPasswordEncoder,never()).matches(password,user.getPassword());
        verify(jwtUtilService,never()).generateToken(user.getUserName(),user.getId(),user.getRoles());

    }

    @Test
    void loginTest_ThrowsInvalidCredentialsExceptionForPassword(){
        String username="Chisimdi";
        String password="Ejoh";
        User user=new User();
        user.setId(1);
        user.setRoles("Admin");
        user.setUserName("Chisimdi");
        user.setPassword("1000");

        when(userRepository.findByUserName(username)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(password,user.getPassword())).thenReturn(false);

        assertThatThrownBy(()->userService.login(username,password)).isInstanceOf(InvalidCredentialsException.class);


        verify(userRepository).findByUserName(username);
        verify(bCryptPasswordEncoder).matches(password,user.getPassword());
        verify(jwtUtilService,never()).generateToken(user.getUserName(),user.getId(),user.getRoles());

    }

}
