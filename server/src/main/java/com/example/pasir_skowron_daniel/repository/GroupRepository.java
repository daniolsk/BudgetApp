package com.example.pasir_skowron_daniel.repository;

import com.example.pasir_skowron_daniel.model.Group;
import com.example.pasir_skowron_daniel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMemberships_User(User user);
}
