package com.example.pasir_skowron_daniel.service;

import jakarta.persistence.EntityNotFoundException;
import com.example.pasir_skowron_daniel.dto.BalanceDto;
import com.example.pasir_skowron_daniel.dto.TransactionDTO;
import com.example.pasir_skowron_daniel.model.Transaction;
import com.example.pasir_skowron_daniel.model.TransactionType;
import com.example.pasir_skowron_daniel.model.User;
import com.example.pasir_skowron_daniel.repository.TransactionRepository;
import com.example.pasir_skowron_daniel.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository=userRepository;
    }

    public List<Transaction> getAllTransactions(){
        User user=getCurrentUser();
        return transactionRepository.findAllByUser(user);
    }
    public User getCurrentUser(){
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("Nie znaleziono użytkownika"));
    }

    public Transaction getTransactionById(Long id){
        return transactionRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Nie znaleziono transakcji o ID: "+id));
    }

    public Transaction saveTransactions(TransactionDTO transactionDTO){

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(TransactionType.valueOf(String.valueOf(transactionDTO.getType())));
        transaction.setTags(transactionDTO.getTags());
        transaction.setNotes(transactionDTO.getNotes());
        transaction.setUser(getCurrentUser());
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(Long id, TransactionDTO transactionDTO){
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Nie znaleziono transakcji o ID: "+id));
        if (!transaction.getUser().getEmail().equals(getCurrentUser().getEmail())){
            throw new SecurityException("Brak dostępu do edycji tej transakcji");
        }
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(TransactionType.valueOf(String.valueOf(transactionDTO.getType())));
        transaction.setTags(transactionDTO.getTags());
        transaction.setNotes(transactionDTO.getNotes());

        return transactionRepository.save(transaction);
    }

    public Transaction removeTransaction(Long id){
        Transaction deletedTransaction = transactionRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Nie znaleziono transakcji o ID: "+id));

        if (!deletedTransaction.getUser().getEmail().equals(getCurrentUser().getEmail())){
            throw new SecurityException("Brak dostępu do edycji tej transakcji");
        }

        try {
            transactionRepository.deleteById(id);
            return deletedTransaction;
        } catch (Exception e) {
            throw new RuntimeException("Błąd przy usuwaniu transakcji "+e);
        }
    }

    public BalanceDto getUserBalance(User user, Float days){
        List<Transaction> userTransactions = transactionRepository.findAllByUser((user));

        if (days != null) {
            long seconds = Math.round(days * 86400); // 86400 sekund = 1 dzień
            LocalDateTime fromDate = LocalDateTime.now().minusSeconds(seconds);
            userTransactions = userTransactions.stream()
                    .filter(t -> t.getTimestamp().isAfter(fromDate))
                    .toList();
        }

        double income =userTransactions.stream()
                .filter(t->t.getType()==TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
        double expense =userTransactions.stream()
                .filter(t->t.getType()==TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();

        return new BalanceDto(income,expense,income-expense);
    }

}
