package org.example.dummybank.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    private static final String SECRET =
            "OfflinePaymentSecureKeyForBankSync1234567890";

    private final Key key =
            Keys.hmacShaKeyFor(
                    SECRET.getBytes()
            );

    public boolean validateToken(
            String token
    ){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e){
            return false;
        }
    }
    public Claims validateAndExtract(
            String token
    ){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String getSubject(
            String token
    ){
        return validateAndExtract(token)
                .getSubject();
    }
}
