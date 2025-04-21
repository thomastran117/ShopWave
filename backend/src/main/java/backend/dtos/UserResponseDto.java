package backend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {
    private String token;
    private String email;
    private String usertype;
    private long userid;

    public UserResponseDto(String token, String email, String usertype, long userid) {
        this.token = token;
        this.email = email;
        this.usertype = usertype;
        this.userid = userid;
    }
}
