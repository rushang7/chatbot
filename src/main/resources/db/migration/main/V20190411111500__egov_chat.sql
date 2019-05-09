DROP TABLE IF EXISTS eg_chat_message;
DROP TABLE IF EXISTS eg_chat_conversation_state;
DROP TABLE IF EXISTS eg_chat_user;

CREATE TABLE eg_chat_user(
    id SERIAL,
    user_id character varying(100),
    mobile_number character varying(50),
    auth_token character varying(100),
    refresh_token character varying(100),
    user_info character varying(1000),
    expires_at bigint,
    PRIMARY KEY (id),
    UNIQUE (user_id)
);

CREATE TABLE eg_chat_conversation_state(
    id SERIAL,
    conversation_id character varying(100),
    active_node_id character varying(100),
    user_id character varying(100),
    active BOOLEAN,
    PRIMARY KEY (id),
    UNIQUE (conversation_id)
);

CREATE TABLE eg_chat_message(
    id SERIAL,
    message_id character varying(50),
    conversation_id character varying(100),
    node_id character varying(100),
    message_content character varying(1000),
    content_type character varying(100),
    PRIMARY KEY (id),
    CONSTRAINT fk_eg_chat_message_conversation FOREIGN KEY (conversation_id) REFERENCES eg_chat_conversation_state
    (conversation_id) ON DELETE CASCADE
);
