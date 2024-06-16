package com.vikash.mobileCaseBackend.repo;

import com.vikash.mobileCaseBackend.model.OrderEntity;
import com.vikash.mobileCaseBackend.model.ProductOrderSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IProductOrderSnapshot extends JpaRepository<ProductOrderSnapshot,Integer> {
    @Query("SELECT s FROM ProductOrderSnapshot s ORDER BY s.snapshotTime DESC")
    List<ProductOrderSnapshot> findTopSnapshot();


}