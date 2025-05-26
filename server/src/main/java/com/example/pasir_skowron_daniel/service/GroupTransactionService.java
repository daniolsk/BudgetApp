package com.example.pasir_skowron_daniel.service;

import com.example.pasir_skowron_daniel.model.*;
import com.example.pasir_skowron_daniel.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import com.example.pasir_skowron_daniel.dto.GroupTransactionDTO;
import com.example.pasir_skowron_daniel.repository.DebtRepository;
import com.example.pasir_skowron_daniel.repository.GroupRepository;
import com.example.pasir_skowron_daniel.repository.MembershipRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupTransactionService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final TransactionRepository transactionRepository;

    public GroupTransactionService(GroupRepository groupRepository, MembershipRepository membershipRepository, DebtRepository debtRepository, TransactionRepository transactionRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.debtRepository = debtRepository;
        this.transactionRepository = transactionRepository;
    }

    public void addGroupTransaction(GroupTransactionDTO dto, User currentUser) {
        Group group = groupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono grupy"));

        List<Membership> members = membershipRepository.findByGroupId(group.getId());
        List<Long> selectedUserIds = dto.getSelectedUserIds();

        if (selectedUserIds == null || selectedUserIds.isEmpty()) {
            throw new IllegalArgumentException("Nie wybrano żadnych użytkowników");
        }

        double amountPerUser = dto.getAmount() / selectedUserIds.size();

        Transaction groupExpense = new Transaction();
        groupExpense.setUser(currentUser);
        groupExpense.setType(TransactionType.EXPENSE);
        groupExpense.setAmount(dto.getAmount());
        groupExpense.setTags("GROUP");
        groupExpense.setNotes(dto.getTitle());
        groupExpense.setTimestamp(java.time.LocalDateTime.now());
        transactionRepository.save(groupExpense);

        for (Membership member : members) {
            User debtor = member.getUser();
            if (!debtor.getId().equals(currentUser.getId()) && selectedUserIds.contains(debtor.getId())) {
                Debt debt = new Debt();
                debt.setDebtor(debtor);
                debt.setCreditor(currentUser);
                debt.setAmount(amountPerUser);
                debt.setGroup(group);
                debt.setTitle(dto.getTitle());
                debtRepository.save(debt);
            }
        }
    }
}

