-- create stored procedure insertUserGroup that is used in Method4.java to insert a new row
-- into the UserGroup table
create procedure insertUserGroup
    @username varchar(16),
    @group_name varchar(50)
as
begin
    insert into UserGroup
        (username, group_name, date_joined)
    values
        (@username, @group_name, GETDATE());
end