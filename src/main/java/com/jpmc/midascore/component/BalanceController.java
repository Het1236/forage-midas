package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam long userId) {
        // Try to find the user by ID
        UserRecord user = userRepository.findById(userId);

        // If user doesn't exist, return balance of 0
        if (user == null) {
            return new Balance(0);
        }

        // If user exists, return their balance
        return new Balance(user.getBalance());
    }
}