-- creates a stored procedure to insert a new group into the ForumGroup table
create procedure insertCreateGroup
    @name varchar(50),
    @owner_name varchar(16)
as
begin
    insert into ForumGroup
        (name, owner_name, date_created)
    values
        (@name, @owner_name, GETDATE());
end