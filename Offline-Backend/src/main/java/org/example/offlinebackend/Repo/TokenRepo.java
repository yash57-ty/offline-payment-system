package org.example.offlinebackend.Repo;

import org.example.offlinebackend.Model.PaymentToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepo extends JpaRepository<PaymentToken, String> {

    List<PaymentToken> findBySenderMobile(String phoneNo);

    List<PaymentToken> findByReceiverMobile(String receiverMobile);
}
