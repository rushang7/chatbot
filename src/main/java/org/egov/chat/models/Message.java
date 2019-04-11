package org.egov.chat.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String message_id;

    private String conversation_id;

    private String node_id;

    private String message_content;

}
