package com.example.pasir_skowron_daniel.repository;

import com.example.pasir_skowron_daniel.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByGroupId(Long groupId);
}
