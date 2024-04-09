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

## Work Done and to be Done

| Work Done | Team Member |
| --- | --- |
| Initial Database Design | All |
| Initial Design Review | All |
| Initial ER Diagram | All |
| Updated ER Diagram | Mike |
<!-- TODO MORE -->

<!-- Report 2 Requirements -->
| Work to be Done | Team Member |
| --- | --- |
| Functional Dependencies | Mike |
| Physical Database Design | Mikey |
| High-Level Outline of Use Cases | Unassigned |
| Desired Applications for the Database | Unassigned |
| Initial Java Application | Mikey |
| UI / Menu System | Unassigned |
| Testing | Unassigned |
