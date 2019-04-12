package org.egov.chat.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConversationStateRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String updateActiveNodeIdQuery = "UPDATE eg_chat_conversation_state SET active_node_id=? WHERE conversation_id=?";

    public int updateActiveNodeForConversation(String conversationId, String activeNodeId) {
        return jdbcTemplate.update(updateActiveNodeIdQuery, activeNodeId, conversationId);
    }

}
