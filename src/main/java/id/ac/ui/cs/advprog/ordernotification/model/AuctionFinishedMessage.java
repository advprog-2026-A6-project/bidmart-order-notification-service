package id.ac.ui.cs.advprog.ordernotification.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionFinishedMessage {
    private Long auctionId;
    private String winnerId;
    private String sellerId;
    private String itemName;
    private Double finalPrice;
    private Long winningBidId;

    public AuctionFinishedMessage(Long auctionId, String winnerId, String itemName, Double finalPrice) {
        this.auctionId = auctionId;
        this.winnerId = winnerId;
        this.itemName = itemName;
        this.finalPrice = finalPrice;
    }
}
