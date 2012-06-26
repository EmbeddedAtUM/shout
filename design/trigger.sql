CREATE TRIGGER Update_Comment_Count IF NOT EXISTS AFTER INSERT ON Shout
  BEGIN
    UPDATE Shout SET Comment_Count = Comment_Count + 1
    WHERE _id = new.Parent
    AND new.Message IS NOT NULL;
  END;

CREATE TRIGGER Update_Reshout_Count IF NOT EXISTS AFTER INSERT ON Shout
  BEGIN
    UPDATE Shout SET Reshout_Count = Reshout_Count + 1
    WHERE _id = new.Parent
    AND new.Message IS NULL;
  END;
