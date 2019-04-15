package org.egov.chat.repository;

import org.egov.chat.models.ConversationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConversationStateRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String updateActiveNodeIdQuery = "UPDATE eg_chat_conversation_state SET active_node_id=? WHERE conversation_id=?";

    private static final String selectConversationStateQuery = "SELECT (active_node_id " +
            ") FROM eg_chat_conversation_state WHERE conversation_id=?";

    public int updateActiveNodeForConversation(String conversationId, String activeNodeId) {
        return jdbcTemplate.update(updateActiveNodeIdQuery, activeNodeId, conversationId);
    }

    public String getActiveNodeIdForConversation(String conversationId) {
        return  (jdbcTemplate.queryForObject(selectConversationStateQuery, new Object[] { conversationId },
                String.class));
    }

}
