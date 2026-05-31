package org.example.offlinebackend.Service;

import org.example.offlinebackend.Model.Chat;
import org.example.offlinebackend.Model.ChatResponse;
import org.example.offlinebackend.Model.UserSession;
import org.example.offlinebackend.Model.Wallet;
import org.example.offlinebackend.Repo.UserSessionRepo;
import org.example.offlinebackend.Repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ResetPin {

    @Autowired
    private UserSessionRepo userSessionRepo;
    @Autowired
    private WalletRepo walletRepo;

    public ChatResponse handel(UserSession userSession, Chat chat) {

        ChatResponse chatResponse = new ChatResponse();

        Wallet wallet = walletRepo.findByphonenumber(chat.getPhone());
        if (wallet == null) {
            chatResponse.setReply("verdict ❌ Wallet not found. Please register first.");
            userSessionRepo.delete(userSession);
            return chatResponse;
        }

        if (userSession.getCurrent_status() == null) {
            userSession.setUser_status(chat.getMessage());
            userSession.setCurrent_status("RESET_MENU");
            userSessionRepo.save(userSession);

            chatResponse.setReply(
                    "4.1 Forgot PIN\n" +
                            "4.2 Change PIN"
            );
            return chatResponse;
        }

        if ("RESET_MENU".equals(userSession.getCurrent_status())
                && "4.1".equals(chat.getMessage())) {

            String otp = String.valueOf(new Random().nextInt(9000) + 1000);

            userSession.setMessage(otp); // store OTP temporarily
            userSession.setCurrent_status("FORGOT_OTP");
            userSessionRepo.save(userSession);

            chatResponse.setReply(
                    "Your OTP is: " + otp + "\n" +
                            "Enter OTP:"
            );
            return chatResponse;
        }

        if ("FORGOT_OTP".equals(userSession.getCurrent_status())) {

            if (!userSession.getMessage().equals(chat.getMessage())) {
                chatResponse.setReply("verdict ❌ Invalid OTP. Try again:");
                userSessionRepo.delete(userSession);
                return chatResponse;
            }

            userSession.setCurrent_status("SET_NEW_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Enter New PIN:");
            return chatResponse;
        }

        if ("SET_NEW_PIN".equals(userSession.getCurrent_status())) {

            userSession.setMessage(chat.getMessage());
            userSession.setCurrent_status("CONFIRM_NEW_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Confirm New PIN:");
            return chatResponse;
        }

        if ("CONFIRM_NEW_PIN".equals(userSession.getCurrent_status())) {

            if (!userSession.getMessage().equals(chat.getMessage())) {
                chatResponse.setReply("❌ PIN mismatch. Enter New PIN again:");
                userSession.setCurrent_status("SET_NEW_PIN");
                userSessionRepo.save(userSession);
                return chatResponse;
            }

            wallet.setPin(chat.getMessage());
            walletRepo.save(wallet);

            userSessionRepo.delete(userSession);

            chatResponse.setReply("✅ PIN reset successfully.");
            return chatResponse;
        }

        if ("RESET_MENU".equals(userSession.getCurrent_status())
                && "4.2".equals(chat.getMessage())) {

            userSession.setCurrent_status("CHANGE_OLD_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Enter Old PIN:");
            return chatResponse;
        }

        if ("CHANGE_OLD_PIN".equals(userSession.getCurrent_status())) {

            if (!wallet.getPin().equals(chat.getMessage())) {
                chatResponse.setReply("verdict ❌ Incorrect Old PIN. Try again:");
                userSessionRepo.delete(userSession);
                return chatResponse;
            }

            userSession.setCurrent_status("CHANGE_NEW_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Enter New PIN:");
            return chatResponse;
        }

        if ("CHANGE_NEW_PIN".equals(userSession.getCurrent_status())) {

            userSession.setMessage(chat.getMessage());
            userSession.setCurrent_status("CONFIRM_CHANGE_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Confirm New PIN:");
            return chatResponse;
        }

        if ("CONFIRM_CHANGE_PIN".equals(userSession.getCurrent_status())) {

            if (!userSession.getMessage().equals(chat.getMessage())) {
                chatResponse.setReply("❌ PIN mismatch. Enter New PIN again:");
                userSession.setCurrent_status("CHANGE_NEW_PIN");
                userSessionRepo.save(userSession);
                return chatResponse;
            }

            wallet.setPin(chat.getMessage());
            walletRepo.save(wallet);
            userSessionRepo.delete(userSession);
            chatResponse.setReply("verdict ✅ PIN changed successfully.");
            return chatResponse;
        }

        chatResponse.setReply("verdict Something went wrong. Please try again.");
        return chatResponse;
    }
}
