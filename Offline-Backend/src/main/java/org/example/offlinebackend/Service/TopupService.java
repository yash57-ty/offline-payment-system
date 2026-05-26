package org.example.offlinebackend.Service;

import org.example.offlinebackend.Model.Dto.BankTopupDTO;
import org.example.offlinebackend.Model.Chat;
import org.example.offlinebackend.Model.ChatResponse;
import org.example.offlinebackend.Model.Dto.BankTopupDTO;
import org.example.offlinebackend.Model.UserSession;
import org.example.offlinebackend.Model.Wallet;
import org.example.offlinebackend.Repo.UserSessionRepo;
import org.example.offlinebackend.Repo.WalletRepo;
import org.example.offlinebackend.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
@Service
public class TopupService {

    private static final int MAX_TOPUP_AMOUNT = 100000;

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private UserSessionRepo userSessionRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    public ChatResponse topUp(UserSession session, Chat chat) {

        ChatResponse response = new ChatResponse();

        if (chat == null || chat.getPhone() == null) {
            response.setReply(" Invalid request.");
            return response;
        }

        if (session == null) {
            response.setReply(" Session expired. Please try again.");
            return response;
        }

        String msg = chat.getMessage() == null ? "" : chat.getMessage().trim();
        Wallet wallet = walletRepo.findByphonenumber(chat.getPhone());

        if (wallet == null) {
            userSessionRepo.delete(session);
            response.setReply("Wallet not found.");
            return response;
        }

        if (wallet.getStatus() != null &&
                !"ACTIVE".equalsIgnoreCase(wallet.getStatus())) {
            userSessionRepo.delete(session);
            response.setReply(" Wallet is not active.");
            return response;
        }

        if (session.getCurrent_status() == null) {
            session.setUser_status(chat.getMessage());
            session.setCurrent_status("TOPUP_PIN");
            userSessionRepo.save(session);
            response.setReply("Enter wallet PIN:");
            return response;
        }

        if ("TOPUP_PIN".equals(session.getCurrent_status())) {

            if (!wallet.getPin().equals(msg)) {
                userSessionRepo.delete(session);
                response.setReply("❌ Wrong PIN. Top-up cancelled.");
                return response;
            }

            session.setCurrent_status("TOPUP_AMOUNT");
            userSessionRepo.save(session);
            response.setReply("Enter amount:");
            return response;
        }

        /* ---------- STEP 3: RECEIVE AMOUNT ---------- */
        if ("TOPUP_AMOUNT".equals(session.getCurrent_status())) {

            int amount;
            try {
                amount = Integer.parseInt(msg);
            } catch (Exception e) {
                response.setReply("❌ Invalid amount. Enter numbers only:");
                return response;
            }

            if (amount <= 0) {
                response.setReply("❌ Amount must be greater than 0.");
                return response;
            }

            if (amount > MAX_TOPUP_AMOUNT) {
                response.setReply("❌ Amount exceeds allowed limit.");
                return response;
            }

            /* ---------- BANK CALL ---------- */
            BankTopupDTO dto = new BankTopupDTO();
            dto.setPhoneNo(chat.getPhone());
            dto.setAmount(amount);

            String jwt = jwtUtil.generateToken(dto.getPhoneNo(),dto.getAmount());
            dto.setJwtToken(jwt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwt);


            HttpEntity<BankTopupDTO> entity = new HttpEntity<>(dto, headers);

            try {
                restTemplate.exchange(
                        "http://localhost:9090/dummy-bank/topup",
                        HttpMethod.POST,
                        entity,
                        Void.class
                );
            } catch (Exception e) {
                userSessionRepo.delete(session);
                response.setReply("❌ Bank server unreachable.");
                return response;
            }

            wallet.setBalance(wallet.getBalance() + amount);
            walletRepo.save(wallet);

            userSessionRepo.delete(session);
            response.setReply("✅ Top-up successful.");
            return response;
        }

        /* ---------- FALLBACK ---------- */
        userSessionRepo.delete(session);
        response.setReply("⚠️ Something went wrong. Please try again.");
        return response;
    }
}

