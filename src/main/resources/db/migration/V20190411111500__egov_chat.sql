CREATE TABLE eg_chat_user(
    user_id character varying(100)
);

CREATE TABLE eg_chat_conversation_state(
    conversation_id character varying(100),
    active_node_id character varying(100),
    user_id character varying(100)
);

CREATE TABLE eg_chat_message(
    message_id character varying(50),
    conversation_id character varying(100),
    node_id character varying(100),
    message_content character varying(1000)
);
