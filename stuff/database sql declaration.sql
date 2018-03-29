CREATE DATABASE KMADeadlineBot;

USE KMADeadlineBot;

CREATE TABLE user (
    user_id BIGINT NOT NULL,
    PRIMARY KEY(user_id)
);
    
CREATE TABLE global_admin(
    user_id BIGINT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES user(user_id)
);

CREATE TABLE community(
    community_name_sn CHAR(255) NOT NULL,
    date_created_dt DATE,
    PRIMARY KEY(community_name_sn)
);
    
CREATE TABLE deadline(
    deadline_id BIGINT NOT NULL AUTO_INCREMENT,
    date_of_deadline DATETIME NOT NULL,
    community_name_sn CHAR(255) NOT NULL,
    PRIMARY KEY(deadline_id),
    FOREIGN KEY(community_name_sn) REFERENCES community(community_name_sn)
);
    
CREATE TABLE deadline_message(
    deadline_message_id BIGINT NOT NULL AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    deadline_id BIGINT NOT NULL,
    PRIMARY KEY(deadline_message_id),
    FOREIGN KEY(deadline_id) REFERENCES deadline(deadline_id)
);

CREATE TABLE deadline_date(
    deadline_date_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    deadline_id BIGINT NOT NULL,
    date DATETIME NOT NULL,
    PRIMARY KEY(deadline_date_id),
    FOREIGN KEY(user_id) REFERENCES user(user_id),
    FOREIGN KEY(deadline_id) REFERENCES deadline(deadline_id)
);

CREATE TABLE user_community(
    user_community_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    community_name_sn CHAR(255) NOT NULL,
    PRIMARY KEY(user_community_id),
    FOREIGN KEY(user_id) REFERENCES user(user_id),
    FOREIGN KEY(community_name_sn) REFERENCES community(community_name_sn)
);
    
CREATE TABLE admin_community(
    admin_community_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    community_name_sn CHAR(255) NOT NULL,
    PRIMARY KEY(admin_community_id),
    FOREIGN KEY(user_id) REFERENCES user(user_id),
    FOREIGN KEY(community_name_sn) REFERENCES community(community_name_sn)
);