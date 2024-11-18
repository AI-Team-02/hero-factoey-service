package com.herofactory.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter @Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing 기능을 사용하기 위한 어노테이션
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    private Long userId;
    private String imageUrl;        // 미리보기 이미지 URL
    private String downloadUrl;     // 실제 다운로드 URL

    @CreatedDate  // 생성 시간 자동 저장
    @Column(updatable = false)  // 수정 시에는 변경되지 않도록 설정
    private LocalDateTime createdAt;

    @LastModifiedDate  // 수정 시간 자동 저장
    private LocalDateTime updatedAt;
}