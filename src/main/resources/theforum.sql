DROP DATABASE IF EXISTS theforum;
CREATE DATABASE theforum;

USE theforum;

CREATE TABLE ForumUser (
    username VARCHAR(16) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    date_created DATETIME NOT NULL,
    CONSTRAINT username_length_check CHECK (LEN(username) >= 3 AND LEN(username) <= 16)
);

CREATE TABLE Thread (
    id INT PRIMARY KEY IDENTITY,
    username VARCHAR(16) NOT NULL,
    title VARCHAR(50) NOT NULL,
    date_created DATETIME NOT NULL,
    FOREIGN KEY (username) REFERENCES ForumUser(username)
);

CREATE TABLE Reply (
    id INT PRIMARY KEY IDENTITY,
    thread_id INT NOT NULL,
    username VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    date_created DATETIME NOT NULL,
    FOREIGN KEY (username) REFERENCES ForumUser(username),
    FOREIGN KEY (thread_id) REFERENCES Thread(id)
);

CREATE TABLE Attachment (
    id INT PRIMARY KEY IDENTITY,
    reply_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    metadata VARCHAR(255) NOT NULL,
    data VARBINARY(MAX) NOT NULL,
    FOREIGN KEY (reply_id) REFERENCES Reply(id),
    CONSTRAINT attachmentname_length_check CHECK (LEN(name) >= 1),
    CONSTRAINT attachmentname_unique_check UNIQUE (reply_id, name)
);

CREATE TABLE ForumGroup (
    name VARCHAR(50) PRIMARY KEY,
    owner_name VARCHAR(16) NOT NULL,
    date_created DATETIME NOT NULL,
    FOREIGN KEY (owner_name) REFERENCES ForumUser(username),
    CONSTRAINT groupname_length_check CHECK (LEN(name) >= 1)
);

CREATE TABLE UserGroup (
    username VARCHAR(16) NOT NULL,
    group_name VARCHAR(50) NOT NULL,
    date_joined DATETIME NOT NULL,
    PRIMARY KEY (username, group_name),
    FOREIGN KEY (username) REFERENCES ForumUser(username),
    FOREIGN KEY (group_name) REFERENCES ForumGroup(name)
);

CREATE TABLE ThreadGroup (
    thread_id INT NOT NULL,
    group_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (thread_id, group_name),
    FOREIGN KEY (thread_id) REFERENCES Thread(id),
    FOREIGN KEY (group_name) REFERENCES ForumGroup(name)
);

-- Indices

CREATE INDEX idx_thread_username ON Thread(username);
CREATE INDEX idx_forumgroup_ownername ON ForumGroup(owner_name);
CREATE INDEX idx_attachment_replyid ON Attachment(reply_id);
CREATE INDEX idx_reply_username ON Reply(username);
CREATE INDEX idx_reply_threadid ON Reply(thread_id);
CREATE INDEX idx_thread_title ON Thread(title);
