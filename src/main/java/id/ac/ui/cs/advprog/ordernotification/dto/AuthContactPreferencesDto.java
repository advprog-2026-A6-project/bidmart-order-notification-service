package id.ac.ui.cs.advprog.ordernotification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthContactPreferencesDto {
    private Long userId;
    private String email;
    private String preferredContactMethod;
    private boolean emailNotificationsEnabled;
    private boolean pushNotificationsEnabled;
}
