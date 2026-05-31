package org.example.offlinebackend.Repo;

import org.example.offlinebackend.Model.PaymentToken;
import org.example.offlinebackend.Model.PaymentTokenFailed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenFailedRepo extends JpaRepository<PaymentTokenFailed, String> {
    List<PaymentTokenFailed> findBySenderMobile(String phoneNo);

    List<PaymentTokenFailed> findByReceiverMobile(String receiverMobile);
}
