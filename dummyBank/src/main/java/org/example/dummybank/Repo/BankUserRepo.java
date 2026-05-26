package org.example.dummybank.Repo;

import org.example.dummybank.Model.BankUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankUserRepo extends JpaRepository<BankUser, String> {
}
