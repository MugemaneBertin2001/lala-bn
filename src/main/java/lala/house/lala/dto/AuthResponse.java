package lala.house.lala.dto;
import lala.house.lala.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String message;
    private User user; 

}


