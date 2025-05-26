package com.example.pasir_skowron_daniel.repository;
import com.example.pasir_skowron_daniel.model.Transaction;
import com.example.pasir_skowron_daniel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUser(User user);
}
