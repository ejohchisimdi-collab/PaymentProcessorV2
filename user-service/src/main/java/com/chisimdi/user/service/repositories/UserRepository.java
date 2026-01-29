package com.chisimdi.user.service.repositories;

import com.chisimdi.user.service.models.User;
import org.mapstruct.control.MappingControl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
Page<User>findByRoles(String roles, Pageable pageable);
User findByUserName(String userName);
}
