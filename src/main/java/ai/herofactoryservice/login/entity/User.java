package ai.herofactoryservice.login.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kakaoId;  // 카카오 고유 ID
    
    @Column(unique = true)
    private String email;
    
    private String name;
    
    private String phoneNumber;

    @CreatedDate
    private LocalDateTime createdAt;
    

    @Builder
    public User(Long kakaoId, String email, String name, String phoneNumber) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}