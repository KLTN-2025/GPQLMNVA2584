package com.kindergarten.management_school.repository;

import com.kindergarten.management_school.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;

@RepositoryRestResource
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a WHERE a.username = :username")
    Optional<Account> findByUsername(@Param("username") String username);

    @Query("SELECT a FROM Account a WHERE a.email = :email")
    Optional<Account> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM Account a WHERE a.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM Account a WHERE a.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END FROM Account a WHERE a.phone = :phone")
    boolean existsByPhone(@Param("phone") String phone);

    @Query("SELECT a FROM Account a")
    java.util.List<Account> findAllAccounts();

    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdCustom(@Param("id") Long id);

    @Query("SELECT COUNT(a) FROM Account a")
    long countAllAccounts();

    @Query("DELETE FROM Account a WHERE a.id = :id")
    void deleteAccountById(@Param("id") Long id);

    @Query(
            value = "SELECT * FROM accounts WHERE phone = :phone LIMIT 1",
            nativeQuery = true
    )
    Optional<Account> findByPhoneNative(@Param("phone") String phone);
}
