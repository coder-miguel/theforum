<!--
    This is the 2nd Report for Team 6's project.
    It is written in markdown and should be converted to PDF before submission.

    Report Requirements:
    Second Project Report: In addition to the details that should be included in all project reports, you
    should also include:
    • a more detailed ER-diagram that incorporates any changes or feedback from the first report
    • discuss functional dependencies and normalization issues
    • the physical database design (tables, indexes, triggers)
    • high-level outline of use cases
    • Moreover, you should describe the desired applications for your database and explain the work
    that each member has done and will do


-->

# Second Project Report

## Team Members

- Mikey Maldonado (mxm1667)
- Mike Zhang (zxz1233)
- Tola Oshomoji (tdo18)

## Database

The Forum

## Problem Statement

This database will keep track of group conversations in a forum. The original posters of each
thread and all the replies to the thread. It will allow users to post topics to the public forum or to
restrict access to a topic by group. Posts restricted to a group will only be viewable and repliable
by members of the group.

## Attempt 1 Feedback

Apr 4 at 9:32am

Good job, and interesting topic. A few notes I have: 

1. I am not sure what the google drive link is supposed to be for, but I do not have access.

2. In the future, please make sure your lines for your diagram are not overlapping. This makes it difficult to understand what it's representing.

3. Your Thread table has a reply_id attribute, but this is not needed as far as I understand since you store thread_id in the Reply table (and Reply is the 'many' table).

4. Your rules state that a Reply can only have 1 or more content attachments, but your diagram suggests that it can have 0 content attachments. If only stuff like files/images are considered attachments (not the text of the reply), then I could see the diagram's assumption working, but if text is considered a content attachment then I could see the rule's assumption working.

5. I would rename your 'user_id' attribute in Group to something like 'owner_id' to distinguish it from users in the group who are not the owner.

6. It will be interesting to see how you handle a thread being able to be part of multiple groups but only viewable by those groups. I'm not entirely sure why this functionality is needed (rather than just having threads belonging to 1 or 0 groups), but I don't see why it's a problem if you would like to implement it this way. Feel free to email me (eme65@case.edu) or visit my office hours if you have any questions or need help moving forward.

- Emil Ekambaram

## Updated ER Diagram

![erd](erd.png)

[Draw.IO Link](https://drive.google.com/file/d/1Mqd3s_5D0qhksFYDah-cYSEmzq6K9eE_/view?usp=sharing)

## Functional Dependencies and Normalization Issues

- Functional Dependencies:
    <!-- TODO -->

- Normalization Issues:
    - `Thread` and `Reply` are similar in structure, but `Reply` is a child of `Thread`. Normally, the starting of a thread begins with a post (with possible attachments), but because `Reply` also has content and attachments, and `Reply` is inherently a child of `Thread`, we decided to keep them separate.  A thread can have no content, so it will be up to the application to enforce that a `User` will post the initial content of the `Thread` as a `Reply`.
    <!-- More? -->

## Physical Database Design
<!-- TODO -->

## High-Level Outline of Use Cases
<!-- TODO -->

## Desired Applications for the Database
<!-- TODO -->

## Work Done
| Work Done | Team Member |
| --- | --- |
| Initial Database Design | All |
| Initial Design Review | All |
| Initial ER Diagram | All |
| Updated ER Diagram | Mike |
<!-- UPDATE THIS -->

## Work to be Done
| Work to be Done | Team Member |
| --- | --- |
| Report | --- |
| Functional Dependencies | Unassigned |
| Physical Database Design | Unassigned |
| High-Level Outline of Use Cases | Unassigned |
| Desired Applications for the Database | Unassigned |
| Report Review | Unassigned |
| Java Application | --- |
| Initial Java Application | Maldonado |
| UI / Menu System | Unassigned |
| Testing | Unassigned |
<!-- TODO MORE -->
