package com.example.pasir_skowron_daniel.service;

import com.example.pasir_skowron_daniel.repository.DebtRepository;
import jakarta.persistence.EntityNotFoundException;
import com.example.pasir_skowron_daniel.dto.GroupDTO;
import com.example.pasir_skowron_daniel.model.Group;
import com.example.pasir_skowron_daniel.model.Membership;
import com.example.pasir_skowron_daniel.model.User;
import com.example.pasir_skowron_daniel.repository.GroupRepository;
import com.example.pasir_skowron_daniel.repository.MembershipRepository;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipService membershipService;
    private final DebtRepository debtRepository;

    public GroupService(GroupRepository groupRepository, MembershipRepository membershipRepository, MembershipService membershipService, DebtRepository debtRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.membershipService = membershipService;
        this.debtRepository = debtRepository;
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @MutationMapping
    public Group createGroup(GroupDTO groupDTO) {
        User owner = membershipService.getCurrentUser();
        Group group = new Group();
        group.setOwner(owner);
        group.setName(groupDTO.getName());
        Group savedGroup = groupRepository.save(group);
        Membership membership = new Membership();
        membership.setGroup(savedGroup);
        membership.setUser(owner);
        membershipRepository.save(membership);
        return savedGroup;
    }

    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new EntityNotFoundException("Grupa o ID " + id + " nie istnieje.");
        }

        debtRepository.deleteAll(debtRepository.findByGroupId(id));
        membershipRepository.deleteAll(membershipRepository.findByGroupId(id));

        groupRepository.deleteById(id);
    }
}

