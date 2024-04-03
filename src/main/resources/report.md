<!--
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

## Updated ER Diagram

![erd](erd.png)

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
<!-- UPDATE THIS -->

## Work to be Done
| Work to be Done | Team Member |
| --- | --- |
| Report | --- |
| Updated ER Diagram | Unassigned |
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
