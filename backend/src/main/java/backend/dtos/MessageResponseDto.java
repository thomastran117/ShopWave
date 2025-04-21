package backend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponseDto {
    public String message;

    public MessageResponseDto(String message) {
        this.message = message;
    }
}
