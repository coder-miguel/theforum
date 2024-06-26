-- Create a trigger that inserts a new row into the UserGroup table 
-- whenever a new row is inserted into the ForumGroup table.
CREATE TRIGGER insertintoUserGroup
ON ForumGroup
AFTER INSERT
AS
BEGIN
    DECLARE @GroupName VARCHAR(50);
    DECLARE @OwnerName VARCHAR(16);

    SELECT @GroupName = name, @OwnerName = owner_name
    FROM inserted;

    INSERT INTO UserGroup (username, group_name, date_joined)
    VALUES (@OwnerName, @GroupName, GETDATE());
END;