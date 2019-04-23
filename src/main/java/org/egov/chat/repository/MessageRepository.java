package org.egov.chat.repository;

import org.egov.chat.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String insertMessageQuery = "INSERT INTO eg_chat_message (message_id, conversation_id, " +
            "node_id, message_content) VALUES (?, ?, ?, ?)";

    private static final String selectMessageForConversationAndNodeQuery = "SELECT * FROM eg_chat_message WHERE " +
            "conversation_id=? AND node_id=?";

    public int insertMessage(Message message) {
        return jdbcTemplate.update(insertMessageQuery,
                message.getMessageId(),
                message.getConversationId(),
                message.getNodeId(),
                message.getMessageContent());
    }

    public Message getMessageForConversationAndNode(String conversationId, String nodeId) {
        return jdbcTemplate.queryForObject(selectMessageForConversationAndNodeQuery, new Object[] { conversationId,
                nodeId }, new BeanPropertyRowMapper<>(Message.class));
    }
}
