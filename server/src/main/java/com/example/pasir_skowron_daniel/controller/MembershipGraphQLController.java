package com.example.pasir_skowron_daniel.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import com.example.pasir_skowron_daniel.dto.GroupResponseDTO;
import com.example.pasir_skowron_daniel.dto.MembershipDTO;
import com.example.pasir_skowron_daniel.dto.MembershipResponseDTO;
import com.example.pasir_skowron_daniel.model.Group;
import com.example.pasir_skowron_daniel.model.Membership;
import com.example.pasir_skowron_daniel.model.User;
import com.example.pasir_skowron_daniel.repository.GroupRepository;
import com.example.pasir_skowron_daniel.repository.MembershipRepository;
import com.example.pasir_skowron_daniel.service.MembershipService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MembershipGraphQLController {

    private final MembershipService membershipService;
    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;

    public MembershipGraphQLController(MembershipService membershipService, MembershipRepository membershipRepository, GroupRepository groupRepository) {
        this.membershipService = membershipService;
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
    }

    @QueryMapping
    public List<MembershipResponseDTO> groupMembers(@Argument Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono grupy o ID: " + groupId));

        return membershipRepository.findByGroupId(group.getId()).stream()
                .map(membership -> new MembershipResponseDTO(
                        membership.getId(),
                        membership.getUser().getId(),
                        membership.getGroup().getId(),
                        membership.getUser().getEmail()
                ))
                .toList();
    }

    @MutationMapping
    public MembershipResponseDTO addMember(@Valid @Argument MembershipDTO membershipDTO) {
        Membership membership = membershipService.addMember(membershipDTO);
        return new MembershipResponseDTO(
                membership.getId(),
                membership.getUser().getId(),
                membership.getGroup().getId(),
                membership.getUser().getEmail()
        );
    }

    @QueryMapping
    public List<GroupResponseDTO> myGroups() {
        User user = membershipService.getCurrentUser();
        return groupRepository.findByMemberships_User(user).stream()
                .map(group -> new GroupResponseDTO(
                        group.getId(),
                        group.getName(),
                        group.getOwner().getId()
                ))
                .toList();
    }
    @MutationMapping
    public Boolean removeMember(@Argument Long membershipId){
        membershipService.removeMember(membershipId);
        return true;
    }
}

