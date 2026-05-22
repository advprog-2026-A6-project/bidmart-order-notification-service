package id.ac.ui.cs.advprog.ordernotification.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionEventMessageTest {

    @Test
    void testAccessorsReadTypedPayloadValues() {
        AuctionEventMessage message = new AuctionEventMessage(
                7L,
                "BidPlaced",
                LocalDateTime.now(),
                Map.of(
                        "auctionId", "11",
                        "sellerId", "seller1",
                        "itemName", "Camera",
                        "bidderId", "bidder1",
                        "newBidderId", "bidder2",
                        "bidAmount", new BigDecimal("100000"),
                        "newBidAmount", 125000,
                        "currentHighestBid", "150000",
                        "participantIds", List.of("seller1", "bidder1", "bidder1", 42)
                )
        );

        assertEquals(7L, message.getEventId());
        assertEquals("BidPlaced", message.getEventType());
        assertEquals(11L, message.getAuctionId());
        assertEquals("seller1", message.getSellerId());
        assertEquals("Camera", message.getItemName());
        assertEquals("bidder1", message.getBidderId());
        assertEquals("bidder2", message.getNewBidderId());
        assertEquals(new BigDecimal("100000"), message.getBidAmount());
        assertEquals(BigDecimal.valueOf(125000.0), message.getNewBidAmount());
        assertEquals(new BigDecimal("150000"), message.getCurrentHighestBid());
        assertEquals(List.of("seller1", "bidder1", "42"), message.getParticipantIds());
    }

    @Test
    void testAccessorsHandleMissingAndBlankPayloadValues() {
        AuctionEventMessage message = new AuctionEventMessage();
        message.setPayload(Map.of(
                "auctionId", "",
                "participantIds", List.of("", "user1", " ")
        ));

        assertNull(message.getAuctionId());
        assertNull(message.getSellerId());
        assertNull(message.getItemName());
        assertNull(message.getBidderId());
        assertNull(message.getNewBidderId());
        assertNull(message.getBidAmount());
        assertNull(message.getNewBidAmount());
        assertNull(message.getCurrentHighestBid());
        assertEquals(List.of("user1"), message.getParticipantIds());
    }

    @Test
    void testAccessorsHandleNullAndUnexpectedPayload() {
        AuctionEventMessage nullPayload = new AuctionEventMessage();
        assertNull(nullPayload.getAuctionId());
        assertTrue(nullPayload.getParticipantIds().isEmpty());

        AuctionEventMessage nonListParticipants = new AuctionEventMessage();
        nonListParticipants.setPayload(Map.of("participantIds", "user1"));
        assertTrue(nonListParticipants.getParticipantIds().isEmpty());
    }
}
