ALTER TABLE referencedata.dispensables ADD COLUMN type text NOT NULL;
ALTER TABLE referencedata.dispensables ALTER COLUMN type SET DEFAULT 'default';
