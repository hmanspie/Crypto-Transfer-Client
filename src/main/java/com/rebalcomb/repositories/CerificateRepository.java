package com.rebalcomb.repositories;

import com.rebalcomb.model.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CerificateRepository extends JpaRepository<Certificate, Long> {
}
