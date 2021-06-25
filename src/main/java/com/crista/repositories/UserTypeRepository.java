package com.crista.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crista.enties.UserType;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType,Long>{
  UserType getByUserid(String userid);
}





































































































































































