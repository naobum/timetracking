package com.kfu.timetracking.repositories;

import com.kfu.timetracking.models.Token;
import com.kfu.timetracking.models.TokenType;
import com.kfu.timetracking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    Optional<Token> findByValueAndType(String value, TokenType type);
    
    List<Token> findByUserAndType(User user, TokenType type);
    
    @Modifying
    @Query("UPDATE Token t SET t.disabled = true WHERE t.user = :user AND t.type = :type")
    void disableAllUserTokensByType(User user, TokenType type);
    
    @Modifying
    @Query("DELETE FROM Token t WHERE t.user = :user")
    void deleteAllByUser(User user);
}
