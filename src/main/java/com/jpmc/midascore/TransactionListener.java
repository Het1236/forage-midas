package com.jpmc.midascore;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionListener {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Autowired
    private IncentiveService incentiveService;  // ADD THIS

    private int count = 0;

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    @Transactional
    public void listen(Transaction transaction) {
        count++;
        System.out.println("\n===========================================");
        System.out.println("Processing Transaction #" + count);
        System.out.println("Amount: " + transaction.getAmount());

        // Validate and process the transaction
        boolean success = processTransaction(transaction);

        if (success) {
            System.out.println("✓ Transaction APPROVED and saved");
        } else {
            System.out.println("✗ Transaction REJECTED");
        }

        // Print Wilbur's current balance after each transaction
        UserRecord wilbur = userRepository.findByName("wilbur");
        if (wilbur != null) {
            System.out.println("\n>>> WILBUR BALANCE: " + wilbur.getBalance());
            System.out.println(">>> ROUNDED DOWN: " + (int)Math.floor(wilbur.getBalance()));
        }

        System.out.println("===========================================\n");
    }

    private boolean processTransaction(Transaction transaction) {
        // Step 1: Find sender and recipient
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        // Step 2: Validate both users exist
        if (sender == null) {
            System.out.println("  Reason: Invalid sender ID - " + transaction.getSenderId());
            return false;
        }

        if (recipient == null) {
            System.out.println("  Reason: Invalid recipient ID - " + transaction.getRecipientId());
            return false;
        }

        System.out.println("  Sender: " + sender.getName() + " (Balance: " + sender.getBalance() + ")");
        System.out.println("  Recipient: " + recipient.getName() + " (Balance: " + recipient.getBalance() + ")");

        // Step 3: Validate sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            System.out.println("  Reason: Insufficient funds");
            return false;
        }

        // Step 4: Get incentive from API
        float incentive = incentiveService.getIncentive(transaction);

        // Step 5: Update balances
        // Sender: deduct transaction amount only (NOT the incentive)
        sender.setBalance(sender.getBalance() - transaction.getAmount());

        // Recipient: add transaction amount PLUS incentive
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentive);

        // Step 6: Save updated users
        userRepository.save(sender);
        userRepository.save(recipient);

        // Step 7: Create and save transaction record WITH incentive
        TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount(), incentive);
        transactionRecordRepository.save(record);

        System.out.println("  New Sender Balance: " + sender.getBalance());
        System.out.println("  New Recipient Balance: " + recipient.getBalance());

        return true;
    }
}