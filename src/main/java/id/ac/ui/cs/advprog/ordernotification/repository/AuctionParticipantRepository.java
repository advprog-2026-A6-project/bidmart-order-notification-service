package id.ac.ui.cs.advprog.ordernotification.repository;

import id.ac.ui.cs.advprog.ordernotification.model.AuctionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionParticipantRepository extends JpaRepository<AuctionParticipant, Long> {
    List<AuctionParticipant> findByAuctionId(Long auctionId);
    Optional<AuctionParticipant> findByAuctionIdAndUserId(Long auctionId, String userId);
}
