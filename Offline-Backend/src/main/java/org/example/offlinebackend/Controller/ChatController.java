package org.example.offlinebackend.Controller;

import org.example.offlinebackend.Model.*;
import org.example.offlinebackend.Repo.TokenFailedRepo;
import org.example.offlinebackend.Repo.TokenRepo;
import org.example.offlinebackend.Repo.TokenSuccessRepo;
import org.example.offlinebackend.Service.ChatService;
import org.example.offlinebackend.Service.WalletSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins="http://localhost:5173")
public class ChatController {

    @Autowired
    TokenRepo  tokenRepo;
    @Autowired
    TokenFailedRepo tokenFailedRepo;
    @Autowired
    TokenSuccessRepo  tokenSuccessRepo;
    @Autowired
    WalletSyncService  walletSyncService;
    @Autowired
    ChatService chatService;

    @PostMapping("api/chat")
     public ChatResponse sendMessage(@RequestBody Chat chat) {
        System.out.println("Hello World");
        return chatService.ChatHandel(chat);
    }

    @PostMapping("user/sync")
    public void syncMessage(@RequestBody UserMobile userMobile) {
        System.out.println(userMobile.getPhoneNo());
        walletSyncService.tokenSync(userMobile);
    }

    @GetMapping("/token/success")
    public List<PaymentTokenSuccess> tokenSuccess(
            @RequestParam String phoneNo
    ) {
        return tokenSuccessRepo.findBySenderMobile(phoneNo);
    }


    @GetMapping("/token/failure")
    public List<PaymentTokenFailed> tokenFailure(
            @RequestParam String phoneNo
    ) {
        return tokenFailedRepo.findBySenderMobile(phoneNo);
    }

    @GetMapping("/token/pending/sent")
    public List<PaymentToken> tokenPendingsent(@RequestParam String phoneNo) {
        return tokenRepo.findBySenderMobile(phoneNo);
    }

    @GetMapping("/token/pending/received")
    public List<PaymentToken> tokenPendingReceived(@RequestParam String phoneNo) {
        return tokenRepo.findByReceiverMobile(phoneNo);
    }

}
