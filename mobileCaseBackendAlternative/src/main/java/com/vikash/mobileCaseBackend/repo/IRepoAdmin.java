package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.Admin;
import com.vikash.mobileCaseBackend.model.AuthenticationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRepoAdmin extends JpaRepository<Admin,Integer> {


    Admin findByAdminEmail(String email);


    //AuthenticationToken findTokenValueByAdmin(String adminEmail);
}
