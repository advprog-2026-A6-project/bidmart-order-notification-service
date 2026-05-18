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
    private String itemName;
    private Double finalPrice;
}
