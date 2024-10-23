//package ai.herofactoryservice.create_game_resource_service.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.io.Serializable;
//import java.time.LocalDateTime;
//import java.util.Base64;
//
//@Entity
//@Table(name = "game_resources")
//@EntityListeners(AuditingEntityListener.class)
//@Getter
//@Setter
//@NoArgsConstructor
//public class GameResourceCreationMessage implements Serializable {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true)
//    private String taskId;
//
//    @Column(nullable = false)
//    private String fileName;
//
//    @Lob
//    @Column(columnDefinition = "LONGTEXT")
//    private String fileContent;
//
//    @Column(columnDefinition = "LONGTEXT")
//    private String prompt;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;
//
//    @CreatedDate
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    private LocalDateTime updatedAt;
//
//    @Enumerated(EnumType.STRING)
//    private ResourceStatus status = ResourceStatus.PROCESSING;
//
//    @JsonIgnore
//    public byte[] getDecodedFileContent() {
//        return Base64.getDecoder().decode(fileContent);
//    }
//
//    @JsonIgnore
//    public void setDecodedFileContent(byte[] fileContent) {
//        this.fileContent = Base64.getEncoder().encodeToString(fileContent);
//    }
//
//    @Builder
//    public GameResourceCreationMessage(String taskId, String fileName,
//                                       String fileContent, String prompt,
//                                       Member member) {
//        this.taskId = taskId;
//        this.fileName = fileName;
//        this.fileContent = fileContent;
//        this.prompt = prompt;
//        this.member = member;
//    }
//}