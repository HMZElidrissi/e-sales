package ma.hmzelidrissi.paymentservice.repository;

import ma.hmzelidrissi.paymentservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCommandeReference(String commandeReference);
}