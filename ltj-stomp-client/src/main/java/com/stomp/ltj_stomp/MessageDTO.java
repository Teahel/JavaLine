package com.stomp.ltj_stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String type;
    private String content;
    private String sender;
    private Long timestamp = System.currentTimeMillis();

    public MessageDTO(String type, String content, String sender) {
        this.type = type;
        this.content = content;
        this.sender = sender;
    }
}