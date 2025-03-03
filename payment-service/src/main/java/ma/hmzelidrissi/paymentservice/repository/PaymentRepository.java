package ma.hmzelidrissi.paymentservice.repository;

import java.util.List;
import ma.hmzelidrissi.paymentservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCommandeReference(String commandeReference);
}