package org.example.offlinebackend.Service;

import org.example.offlinebackend.Model.*;
import org.example.offlinebackend.Repo.UserSessionRepo;
import org.example.offlinebackend.Repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WalletService {

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private UserSessionRepo userSessionRepo;

    @Autowired
    private TokenService tokenService;

    public ChatResponse transaction(UserSession session, Chat chat) {

        ChatResponse response = new ChatResponse();
        if(session.getPin_attempts()==null){
            session.setPin_attempts(0);
        }


        Wallet sender=walletRepo.findByphonenumber(chat.getPhone());
        if(sender==null){
            response.setReply("first you register and again restart type bank");
            userSessionRepo.delete(session);
            return response;
        }

        if(session.getCurrent_status() == null) {
            session.setCurrent_status("SET_AMOUNT");
            userSessionRepo.save(session);
            response.setReply("Enter Amount");
            return response;
        }

        if ("SET_AMOUNT".equals(session.getCurrent_status())) {
            int amount;
            try {
                amount = Integer.parseInt(chat.getMessage());
            } catch (Exception e) {
                response.setReply("Invalid Amount");
                return response;
            }
            if (sender.getBalance() < amount) {
                response.setReply("Insufficient Balance"+"\n"+"your current balance:"+sender.getBalance() +"you again type bank to restart");
                userSessionRepo.delete(session);
                return response;
            }

            session.setAmount(amount);
            session.setCurrent_status("SET_MOBILE");
            userSessionRepo.save(session);
            response.setReply("Enter receiver mobile number");
            return response;
        }

        if ("SET_MOBILE".equals(session.getCurrent_status())) {

            session.setReceiver_mobile(chat.getMessage());
            session.setCurrent_status("SET_PIN");
            userSessionRepo.save(session);

            response.setReply("Enter PIN");
            return response;
        }

        if ("SET_PIN".equals(session.getCurrent_status())) {


            if (!sender.getPin().equals(chat.getMessage())) {

                if (sender.getPinAttempts() == null) {
                    sender.setPinAttempts(0);
                }

                sender.setPinAttempts(sender.getPinAttempts() + 1);

                if (sender.getPinAttempts() >= 3) {
                    sender.setPinBlockedUntil(
                            LocalDateTime.now().plusHours(24)
                    );
                    walletRepo.save(sender);
                    userSessionRepo.delete(session);
                    response.setReply(
                            "❌ Wrong PIN entered 3 times.\n" +
                                    "You are blocked for 24 hours."
                    );
                    return response;
                }
                walletRepo.save(sender);
                response.setReply(
                        "Wrong PIN. Attempts left: " +
                                (3 - sender.getPinAttempts())
                );
                return response;
            }
            sender.setPinAttempts(0);
            sender.setPinBlockedUntil(null);
            int amount = session.getAmount();
            sender.setBalance(sender.getBalance() - amount);
            walletRepo.save(sender);
            PaymentToken token = tokenService.generateToken(
                    sender,
                    session.getReceiver_mobile(),
                    amount
            );
            userSessionRepo.delete(session);
            response.setReply(
                    "Payment Token: " + token.getTokenId()+
                            "\nAmount: " + amount +
                            "\nStatus: CREATED"
            );
            return response;
        }
        return response;
    }
}
