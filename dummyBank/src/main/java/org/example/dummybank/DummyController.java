package org.example.dummybank;

import io.jsonwebtoken.Claims;
import org.example.dummybank.Model.BankTopupDTO;
import org.springframework.beans.factory.annotation.Autowired;

import org.example.dummybank.Model.BankUser;
import org.example.dummybank.Repo.BankUserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/dummy-bank")
public class DummyController {

    private final BankUserRepo bankUserRepo;

    public DummyController(BankUserRepo bankUserRepo) {
        this.bankUserRepo = bankUserRepo;
    }

    @Autowired
    org.example.dummybank.Util.JwtUtil jwtUtil;

    @PostMapping("/verify")
    public org.springframework.http.ResponseEntity<?> verify(
            @RequestBody PaymentTokenDTO[] tokens,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return org.springframework.http.ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String tokenStr = authHeader.substring(7);
        if (!jwtUtil.validateToken(tokenStr)) {
            return org.springframework.http.ResponseEntity.status(401).body("Invalid Token");
        }


        BankResponse[] responses = new BankResponse[tokens.length];

        for (int i = 0; i < tokens.length; i++) {

            PaymentTokenDTO token = tokens[i];
            BankResponse res = new BankResponse();
            res.setTokenId(token.getTokenId());

            BankUser receiver =
                    bankUserRepo.findById(token.getReceiverMobile()).orElse(null);

            if (receiver == null) {
                res.setStatus("FAILED");
                responses[i] = res;
                continue;
            }
            Random random = new Random();
            int  number = random.nextInt(100);
            if(number%2==0){
                res.setStatus("SUCCESS");
                receiver.setBalance(receiver.getBalance() + token.getAmount());
                bankUserRepo.save(receiver);
            }else {
                res.setStatus("FAILED");
            }
            responses[i] = res;
        }
        return org.springframework.http.ResponseEntity.ok(responses);
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topup(

            @RequestBody BankTopupDTO dto

    ){

        try {

            Claims claims =

                    jwtUtil.validateAndExtract(
                            dto.getJwtToken()
                    );

            String phone =

                    claims.getSubject();

            Integer amount =

                    claims.get(
                            "amount",
                            Integer.class
                    );

            BankUser user =

                    bankUserRepo
                            .findById(phone)
                            .orElse(null);

            if(user == null){

                return ResponseEntity
                        .badRequest()
                        .body("User not found");
            }

            user.setBalance(
                    user.getBalance() - amount
            );

            bankUserRepo.save(user);

            return ResponseEntity.ok("SUCCESS");

        } catch (Exception e){

            return ResponseEntity
                    .status(401)
                    .body("INVALID TOKEN");
        }
    }
}
