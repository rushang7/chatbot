package org.egov.chat.repository;

import org.egov.chat.models.ConversationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConversationStateRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String insertNewConversationQuery = "INSERT INTO eg_chat_conversation_state " +
            "(conversation_id, active_node_id, user_id, active, question_details) VALUES (?, ?, ?, ?, ?)";

    private static final String updateActiveNodeIdQuery = "UPDATE eg_chat_conversation_state SET active_node_id=? " +
            "WHERE conversation_id=?";

    private static final String updateActiveStateForConversationQuery = "UPDATE eg_chat_conversation_state SET " +
            "active=FALSE WHERE conversation_id=?";

    private static final String selectActiveNodeIdForConversationStateQuery = "SELECT (active_node_id " +
            ") FROM eg_chat_conversation_state WHERE conversation_id=?";

    private static final String selectConversationStateForUserIdQuery = "SELECT * FROM eg_chat_conversation_state WHERE " +
            "user_id=? AND active=TRUE";

    public int updateActiveNodeForConversation(String conversationId, String activeNodeId) {
        return jdbcTemplate.update(updateActiveNodeIdQuery, activeNodeId, conversationId);
    }

    public int markConversationInactive(String conversationId) {
        return jdbcTemplate.update(updateActiveStateForConversationQuery, conversationId);
    }

    public String getActiveNodeIdForConversation(String conversationId) {
        return  (jdbcTemplate.queryForObject(selectActiveNodeIdForConversationStateQuery, new Object[] { conversationId },
                String.class));
    }

    public ConversationState getConversationStateForUserId(String userId) {
        try {
            return jdbcTemplate.queryForObject(selectConversationStateForUserIdQuery, new Object[]{userId},
                    new BeanPropertyRowMapper<>(ConversationState.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int insertNewConversation(ConversationState conversationState) {
        return jdbcTemplate.update(insertNewConversationQuery,
                conversationState.getConversationId(),
                conversationState.getActiveNodeId(),
                conversationState.getUserId(),
                conversationState.isActive());
    }


}
