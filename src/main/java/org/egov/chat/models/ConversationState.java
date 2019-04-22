package org.egov.chat.models;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationState {

    private String conversationId;

    private String activeNodeId;

    private String userId;

    private boolean active;
}
