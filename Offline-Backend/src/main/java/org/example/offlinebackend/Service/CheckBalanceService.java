package org.example.offlinebackend.Service;

import org.example.offlinebackend.Model.Chat;
import org.example.offlinebackend.Model.ChatResponse;
import org.example.offlinebackend.Model.UserSession;
import org.example.offlinebackend.Model.Wallet;
import org.example.offlinebackend.Repo.UserSessionRepo;
import org.example.offlinebackend.Repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckBalanceService {

    @Autowired
    private UserSessionRepo userSessionRepo;

    @Autowired
    private WalletRepo walletRepo;

    public ChatResponse handel(UserSession userSession, Chat chat) {
        ChatResponse chatResponse = new ChatResponse();
        Wallet wallet = walletRepo.findByphonenumber(chat.getPhone());
        if (wallet == null) {
            chatResponse.setReply("❌ Wallet not found. Please register first.");
            userSessionRepo.delete(userSession);
            return chatResponse;
        }
        if (userSession.getCurrent_status() == null) {

            userSession.setUser_status(chat.getMessage());
            userSession.setCurrent_status("WAITING_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Enter Wallet PIN:");
            return chatResponse;
        }
        if ("WAITING_PIN".equals(userSession.getCurrent_status())) {

            if (!wallet.getPin().equals(chat.getMessage())) {
                chatResponse.setReply("❌ Wrong PIN. Try again later.");
                userSessionRepo.delete(userSession);
                return chatResponse;
            }
            chatResponse.setReply(
                    "💰 Available Balance: ₹" + wallet.getBalance()
            );
            userSessionRepo.delete(userSession);
            return chatResponse;
        }
        chatResponse.setReply("Something went wrong. Please try again.");
        userSessionRepo.delete(userSession);
        return chatResponse;
    }
}
