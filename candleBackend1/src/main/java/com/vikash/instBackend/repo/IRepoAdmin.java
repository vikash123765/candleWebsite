package com.vikash.instBackend.repo;

import com.vikash.instBackend.model.Admin;
import com.vikash.instBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRepoAdmin extends JpaRepository<Admin,Integer> {


    Admin findByAdminEmail(String email);
}
