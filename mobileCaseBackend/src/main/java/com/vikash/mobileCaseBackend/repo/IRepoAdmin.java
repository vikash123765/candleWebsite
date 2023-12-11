package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRepoAdmin extends JpaRepository<Admin,Integer> {


    Admin findByAdminEmail(String email);
}
