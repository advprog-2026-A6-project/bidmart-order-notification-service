package id.ac.ui.cs.advprog.ordernotification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Long auctionId;
    private String itemName;
    private Double totalPrice;
    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}