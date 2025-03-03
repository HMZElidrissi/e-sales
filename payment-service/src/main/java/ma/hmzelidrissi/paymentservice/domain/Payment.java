package ma.hmzelidrissi.paymentservice.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private String commandeReference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Transaction> transactions = new ArrayList<>();

  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Refund> refunds = new ArrayList<>();

  @PrePersist
  public void prePersist() {
    createdAt = LocalDateTime.now();
  }

  public void addTransaction(Transaction transaction) {
    transactions.add(transaction);
    transaction.setPayment(this);
  }

  public void addRefund(Refund refund) {
    refunds.add(refund);
    refund.setPayment(this);
  }
}
