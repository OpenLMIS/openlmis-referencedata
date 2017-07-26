ALTER TABLE permission_strings RENAME TO right_assignments;

ALTER TABLE right_assignments RENAME COLUMN value TO rightname;

ALTER TABLE right_assignments ADD FOREIGN KEY (rightname) REFERENCES rights (name);

ALTER TABLE right_assignments ADD COLUMN facilityid uuid REFERENCES facilities;

ALTER TABLE right_assignments ADD COLUMN programid uuid REFERENCES programs;