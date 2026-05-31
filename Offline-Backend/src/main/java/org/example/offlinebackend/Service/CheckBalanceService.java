package org.example.offlinebackend.Service;

import org.example.offlinebackend.Model.Chat;
import org.example.offlinebackend.Model.ChatResponse;
import org.example.offlinebackend.Model.Dto.BankBalanceRes;
import org.example.offlinebackend.Model.UserSession;
import org.example.offlinebackend.Model.Wallet;
import org.example.offlinebackend.Repo.UserSessionRepo;
import org.example.offlinebackend.Repo.WalletRepo;
import org.example.offlinebackend.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CheckBalanceService {

    @Autowired
    private UserSessionRepo userSessionRepo;

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RestTemplate restTemplate;

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
            userSession.setCurrent_status("WAITING_PIN");
            userSessionRepo.save(userSession);

            chatResponse.setReply("Enter Wallet PIN:");
            return chatResponse;
        }
        if ("WAITING_PIN".equals(userSession.getCurrent_status())) {
            System.out.println("hello");
            if (!wallet.getPin().equals(chat.getMessage())) {
                chatResponse.setReply("verdict ❌ Wrong PIN. Try again later.");
                userSessionRepo.delete(userSession);
                return chatResponse;
            }
            String jwtToken =
                    jwtUtil.generateToken(
                            chat.getPhone(),
                            0
                    );
            HttpHeaders headers =
                    new HttpHeaders();
            headers.set(
                    "Authorization",
                    "Bearer " + jwtToken
            );
            HttpEntity<String> entity =
                    new HttpEntity<>(
                            "",
                            headers
                    );
            ResponseEntity<BankBalanceRes> responseEntity =
                    restTemplate.exchange(
                            "http://localhost:9090/dummy-bank/checkbalance",
                            HttpMethod.POST,
                            entity,
                            BankBalanceRes.class
                    );
            BankBalanceRes body =
                    responseEntity.getBody();
            chatResponse.setReply(
                    "verdict 🏦 Bank Balance: ₹" +
                            body.getAmount() +
                            "\n💰 Wallet Balance: ₹" +
                            wallet.getBalance()
            );
            userSessionRepo.delete(userSession);
            return chatResponse;
        }
        chatResponse.setReply("verdict Something went wrong. Please try again.");
        userSessionRepo.delete(userSession);
        return chatResponse;
    }
}
