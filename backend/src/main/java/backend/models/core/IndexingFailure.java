package backend.models.core;

import backend.models.enums.IndexingFailureStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "indexing_failures", indexes = {
        @Index(name = "idx_indexing_failure_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class IndexingFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String documentType;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = true)
    private Long companyId;

    @Column(nullable = false, length = 10)
    private String operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IndexingFailureStatus status = IndexingFailureStatus.PENDING;

    @Column(nullable = true, length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = true)
    private Instant lastAttemptAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
