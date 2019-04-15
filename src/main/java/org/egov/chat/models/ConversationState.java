package org.egov.chat.models;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationState {

    private String conversation_id;

    private String active_node_id;

    private String user_id;

    private boolean active;
}
