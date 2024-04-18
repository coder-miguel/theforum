# Second Project Report

<!-- This is the 2nd Report for Team 6's project. It is written in markdown language and should be converted to PDF before submission. -->

## Team Members

- Mikey Maldonado (mxm1667)
- Mike Zhang (zxz1233)
- Tola Oshomoji (tdo18)

## Database Name

The Forum

## Problem Statement

This database will keep track of group conversations in a forum. The original posters of each thread and all the replies to the thread. It will allow users to post topics to the public forum or to restrict access to a topic by group. Posts restricted to a group will only be viewable and repliable by members of the group.

## Updated ER-Diagram

![erd](erd.png)

<!-- Link to edit the ERD:
https://drive.google.com/file/d/1Mqd3s_5D0qhksFYDah-cYSEmzq6K9eE_/view?usp=sharing
-->

## Functional Dependencies and Normalization Issues

### Functional Dependencies

- `ForumUser.username` -> all attributes in `ForumUser`

- `Reply.id` -> all attributes in `Reply` and `Thread`

- `Attachment.id` -> all attributes in `Attachment` and `Reply`

- `Thread.id` -> all attributes in `Thread` and `ForumUser`

- `ForumGroup.name` -> all attributes in `ForumGroup` and `ForumUser`

- `ForumGroup.username` , `ForumGroup.group_name` -> all attributes in `UserGroup`, `ForumUser` and `ThreadGroup`

- `ThreadGroup.thread_id` , `ThreadGroup.group_name` -> all attributes in `ThreadGroup`, `Thread` and `UserGroup`


### Normalization Issues

- `Thread` and `Reply` are similar in structure, but replies are generally child nodes of a thread. Normally, the starting of a thread begins with a post (with possibly its own attachments), but because `Reply` also has text content and attachments, we decided to have thread not have any content other than a title.  The database will enforce that a new thread must have an initial post as a `Reply`.
    <!-- More? -->

## Physical Database Design


### Tables

```sql
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
    date_created DATETIME NOT NULL,
    owner_name VARCHAR(16) NOT NULL
    FOREIGN KEY (owner_name) REFERENCES ForumUser(username),
    CONSTRAINT groupname_length_check CHECK (LEN(name) >= 1)
);

CREATE TABLE UserGroup (
    username VARCHAR(16) NOT NULL,
    group_name VARCHAR(50) NOT NULL,
    date_joined DATETIME NOT NULL,
    PRIMARY KEY (user_id, group_name),
    FOREIGN KEY (username) REFERENCES ForumUser(username),
    FOREIGN KEY (group_name) REFERENCES ForumGroup(id)
);

CREATE TABLE ThreadGroup (
    thread_id INT NOT NULL,
    group_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (thread_id, group_name),
    FOREIGN KEY (thread_id) REFERENCES Thread(id),
    FOREIGN KEY (group_name) REFERENCES ForumGroup(id)
);
```

### Indexes

```sql
CREATE INDEX idx_thread_username ON Thread(username);
CREATE INDEX idx_forumgroup_ownername ON ForumGroup(owner_name);
CREATE INDEX idx_attachment_replyid ON Attachment(reply_id);
CREATE INDEX idx_reply_username ON Reply(username);
CREATE INDEX idx_reply_threadid ON Reply(thread_id);
CREATE INDEX idx_thread_title ON Thread(title);
```

### Triggers

- A trigger that automatically adds the creator of the group to the `ForumGroup`

- A trigger to enforce that a `ForumUser` can only reply to a `Thread` that is in a `ForumGroup` that they are a member of.

- A trigger to insert the timestamp of when entries are inserted into the `Thread`, `Reply`, `ForumUser`, and `ForumGroup`.

- A trigger to enforce that a `ForumUser` can only edit or delete their own `Thread`, `Reply`, and `Attachment`.

- A trigger to delete all `Attachment` entries when a `Reply` is deleted.

- A trigger to delete all `Reply` entries when a `Thread` is deleted.

- A trigger to remove all `ForumUsers` and `Threads` from a `ForumGroup` when the owner deletes a `ForumGroup`.

## High-Level Outline of Use Cases

```sql 
-- A user can create a group
INSERT INTO ForumUser (username, password, date_created)
VALUES ('user1', 'password1',  GETDATE())
INSERT INTO ForumGroup (name, owner_name, date_created)
SELECT 'CSDS 314 Q/A', F.username, GETDATE()
FROM ForumUser F
WHERE F.username = 'user1'

-- A user can join an existing group 
INSERT INTO ForumUser (username, password, date_created)
VALUES ('user2', 'password2',  GETDATE())
INSERT INTO UserGroup (username, group_name, date_joined)
SELECT F.username, G.name, GETDATE()
FROM ForumUser F, ForumGroup G
WHERE F.username = 'user2' and G.name = 'CSDS 314 Q/A'

-- A user can view all the thread IDs related to their group 
SELECT thread_id
FROM ThreadGroup
WHERE group_name = 'group the user is in' 

-- A user can view all memeber of the group they are in 
SELECT username
FROM UserGroup 
WHERE group_name = 'group the user is in' 

-- A user can see all threads created by members of their group 
SELECT T.username 
FROM Thread T inner join ThreadGroup G
ON (T.ID = G.thread_id)
WHERE group_name = 'group the user is in'
```


## High-Level Outline of the forum

- A user can start zero or many threads.

- A user can have zero or many replies to a thread.

- A user can own zero or many groups.

- A user can be a member of zero or many groups.

- A user can edit or delete their own threads, replies, and groups.

- A thread can have one or many replies.

- A thread can be in zero or many groups. A thread is considered public if it is in zero groups.

- A reply can have zero or many attachments. Each attachment is associated with one reply.

## Desired Applications for the Database

- A Java CLI Application that allows users to interact with the forum.

- A web application that allows users to interact with the forum, sign up, log in, create threads, reply to threads, view threads, and create and join groups.  You can use it like Stack Overflow or a fandom wiki forum.

## Work Done and to be Done

| Work Done | Team Member |
| --- | --- |
| Initial Database Design | All |
| Initial Design Review | All |
| Initial ER Diagram | All |
| Updated ER Diagram | Mike |
| High-Level Outline of Use Cases | Tola |
| Desired Applications for the Database | Tola |
| Initial Java Application | Mikey |
| Functional Dependencies | Mike |
| Physical Database Design | Mikey |

<!-- Report 2 Requirements -->
| Work to be Done | Team Member |
| --- | --- |
| Java Functions | Tola |
| UI / Menu System | Unassigned |
| JUnit Testing | Unassigned |
