package lala.house.lala.dto;

import lala.house.lala.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompleteRegistrationRequest {
    private String email;
    private UserRole role;
}
