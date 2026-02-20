package id.ac.ui.cs.advprog.ordernotification.feature;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String type;

    public Notification() {}

    public Notification(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public String getType() { return type; }

    public void setId(Long id) { this.id = id; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
}