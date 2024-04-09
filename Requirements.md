# Requirements for Report 2

## Attempt 1 Feedback

Apr 4 at 9:32am

Good job, and interesting topic. A few notes I have: 

1. I am not sure what the google drive link is supposed to be for, but I do not have access.

2. In the future, please make sure your lines for your diagram are not overlapping. This makes it difficult to understand what it's representing.

3. Your Thread table has a `reply_id` attribute, but this is not needed as far as I understand since you store `thread_id` in the Reply table (and Reply is the 'many' table).

4. Your rules state that a Reply can only have 1 or more content attachments, but your diagram suggests that it can have 0 content attachments. If only stuff like files/images are considered attachments (not the text of the reply), then I could see the diagram's assumption working, but if text is considered a content attachment then I could see the rule's assumption working.

5. I would rename your `user_id` attribute in Group to something like `owner_id` to distinguish it from users in the group who are not the owner.

6. It will be interesting to see how you handle a thread being able to be part of multiple groups but only viewable by those groups. I'm not entirely sure why this functionality is needed (rather than just having threads belonging to 1 or 0 groups), but I don't see why it's a problem if you would like to implement it this way.

\- Emil E.

## Report 2 Requirements

Second Project Report: In addition to the details that should be included in all project reports, you should also include:

- a more detailed [ER-diagram](README.md#updated-er-diagram) that incorporates any changes or feedback from the first report

- discuss [functional dependencies and normalization issues](README.md#functional-dependencies-and-normalization-issues)

- the [physical database design](README.md#physical-database-design) (tables, indexes, triggers)

- [high-level outline of use cases](README.md#high-level-outline-of-use-cases)

- moreover, you should describe the [desired applications](README.md#desired-applications-for-the-database) for your database and explain the [work that each member has done and will do](README.md#work-done-and-to-be-done)
