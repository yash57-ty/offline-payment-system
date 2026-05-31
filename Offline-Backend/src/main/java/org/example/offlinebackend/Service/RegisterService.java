package org.example.offlinebackend.Service;

import org.example.offlinebackend.Model.*;
import org.example.offlinebackend.Repo.ACCInformationRepo;
import org.example.offlinebackend.Repo.UserSessionRepo;
import org.example.offlinebackend.Repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {
    @Autowired
    private ACCInformationRepo accInformationRepo;
    @Autowired
    private UserSessionRepo userSessionRepo;
    @Autowired
    private WalletRepo walletRepo;
    public ChatResponse Register(UserSession userSession, Chat chat) {
        ChatResponse chatResponse = new ChatResponse();
        Wallet wallet1 = walletRepo.findByphonenumber(chat.getPhone());
        if(wallet1 != null) {
                chatResponse.setReply("verdict already register");
                userSessionRepo.delete(userSession);
                return chatResponse;
        }

        if (userSession.getCurrent_status() == null) {
            userSession.setCurrent_status("WAITING_DEBITCARD");
            userSessionRepo.save(userSession);
            chatResponse.setReply("Enter Debit Card Number:");
            return chatResponse;
        }

        if ("WAITING_DEBITCARD".equals(userSession.getCurrent_status())) {

            AccInformation accInformation = accInformationRepo
                    .findById(userSession.getPhone_number())
                    .orElse(null);

            if (accInformation == null ||
                    !accInformation.getDebitCard_number().equals(chat.getMessage())) {

                chatResponse.setReply(
                        "verdict Debit card not found or KYC not completed"
                );
                userSessionRepo.delete(userSession);
                return chatResponse;
            }

            userSession.setMessage(chat.getMessage());
            userSession.setCurrent_status("DEBIT_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Enter Debit Card PIN:");
            return chatResponse;
        }

        /* STEP 3: Receive Debit PIN */
        if ("DEBIT_PIN".equals(userSession.getCurrent_status())) {

            AccInformation accInformation = accInformationRepo
                    .findById(userSession.getPhone_number())
                    .orElse(null);

            if (!accInformation.getDebitCard_Pin().equals(chat.getMessage())) {
                chatResponse.setReply("verdict Wrong Debit Card PIN. Try again.");
                userSessionRepo.delete(userSession);
                return chatResponse;
            }

            userSession.setCurrent_status("SET_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Set Wallet PIN:");
            return chatResponse;
        }

        if ("SET_PIN".equals(userSession.getCurrent_status())) {

            userSession.setMessage(chat.getMessage());
            userSession.setCurrent_status("CONFIRM_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Confirm Wallet PIN:");
            return chatResponse;
        }

        if ("CONFIRM_PIN".equals(userSession.getCurrent_status())) {

            if (!userSession.getMessage().equals(chat.getMessage())) {
                chatResponse.setReply("❌ PIN mismatch. Set Wallet PIN again:");
                userSession.setCurrent_status("SET_PIN");
                userSessionRepo.save(userSession);
                return chatResponse;
            }

            Wallet wallet = new Wallet();
            wallet.setPhonenumber(chat.getPhone());
            wallet.setPin(chat.getMessage());
            wallet.setBalance(0);
            wallet.setStatus("ACTIVE");
            userSession.setUser_status(null);

            AccInformation acc = accInformationRepo
                    .findById(chat.getPhone())
                    .orElse(null);

            wallet.setAccInformation(acc);
            walletRepo.save(wallet);
            userSessionRepo.delete(userSession);
            chatResponse.setReply("verdict 🎉 Registration Successful!\nYour wallet is active.");
            return chatResponse;
        }
        chatResponse.setReply("verdict Something went wrong. Please try again");
        if(userSession!= null)userSessionRepo.delete(userSession);
        return chatResponse;
    }
}
