package com.chisimdi.user.service.mappers;

import com.chisimdi.user.service.models.User;
import com.chisimdi.user.service.models.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);
    List<UserDTO>toUserDTOList(List<User>users);

}
