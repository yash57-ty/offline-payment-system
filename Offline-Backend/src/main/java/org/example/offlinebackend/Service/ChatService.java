package org.example.offlinebackend.Service;

import org.example.offlinebackend.Model.*;
import org.example.offlinebackend.Repo.ACCInformationRepo;
import org.example.offlinebackend.Repo.UserSessionRepo;
import org.example.offlinebackend.Repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.Random;

@Service
public class ChatService {

    @Autowired
    UserSessionRepo userSessionRepo;

    @Autowired
    WalletRepo walletRepo;

    @Autowired
    RegisterService registerService;

    @Autowired
    WalletService walletService;

    @Autowired
    TopupService topupService;

    @Autowired
    ResetPin resetPin;

    @Autowired
    CheckBalanceService checkBalanceService;

    public ChatResponse ChatHandel(Chat chat) {

        Wallet wallet = walletRepo.findByphonenumber(chat.getPhone());

        if (wallet != null &&
                wallet.getPinBlockedUntil() != null &&
                wallet.getPinBlockedUntil().isAfter(java.time.LocalDateTime.now())) {

            return new ChatResponse(
                    "❌ You are blocked due to wrong PIN attempts.\n" +
                            "Please try again after 24 hours."
            );
        }

        UserSession session = userSessionRepo
                .findById(chat.getPhone())
                .orElse(null);

        if (session == null) {
            UserSession newSession = new UserSession();
            newSession.setPhone_number(chat.getPhone());
            newSession.setCaptchaVerified(false);

            String captcha = String.valueOf(1000 + new Random().nextInt(9000));
            newSession.setCaptchaCode(captcha);

            userSessionRepo.save(newSession);

            return new ChatResponse(
                    "Please reply with this number: " + captcha
            );
        }


        if (!Boolean.TRUE.equals(session.getCaptchaVerified())) {
            if (chat.getMessage().equals(session.getCaptchaCode())) {
                session.setCaptchaVerified(true);
                session.setCaptchaCode(null);
                userSessionRepo.save(session);
                return new ChatResponse(getMainMenu());
            } else {
                userSessionRepo.delete(session);
                return new ChatResponse("verdict Wrong number. Try again:");
            }
        }

        if (session.getUser_status() == null) {
            session.setUser_status(chat.getMessage());
            userSessionRepo.save(session);
        }

        String option = session.getUser_status();

        if ("1".equals(option)) {
            return registerService.Register(session, chat);
        }
        if ("2".equals(option)) {
            return walletService.transaction(session, chat);
        }
        if ("3".equals(option)) {
            return topupService.topUp(session, chat);
        }

        if ("4".equals(option)) {
            return resetPin.handel(session, chat);
        }

        if ("5".equals(option)) {
           return checkBalanceService.handel(session, chat);
        }

        userSessionRepo.delete(session);
        return new ChatResponse("verdict:choose correct option");
    }
    private String getMainMenu() {
        return "Welcome to Offline Wallet Service\n" +
                "--------------------------------\n" +
                "1. Register\n" +
                "2. Money Transfer\n" +
                "3. Top Up\n" +
                "4. Reset PIN\n" +
                "5. Check Balance\n" +
                "Reply with option number:";
    }
}
