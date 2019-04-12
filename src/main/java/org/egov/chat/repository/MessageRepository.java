package org.egov.chat.repository;

import org.egov.chat.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String insertMessageQuery = "INSERT INTO eg_chat_message (message_id, conversation_id, node_id, message_content) " +
            "VALUES (?, ?, ?, ?)";

    public int insertMessage(Message message) {
        return jdbcTemplate.update(insertMessageQuery,
                message.getMessage_id(),
                message.getConversation_id(),
                message.getNode_id(),
                message.getMessage_content());
    }


}
