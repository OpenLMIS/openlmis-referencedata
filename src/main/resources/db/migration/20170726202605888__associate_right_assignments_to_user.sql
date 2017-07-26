ALTER TABLE right_assignments ADD COLUMN userid uuid REFERENCES users;

ALTER TABLE right_assignments DROP COLUMN roleassignmentid;

UPDATE right_assignments SET userid = '35316636-6264-6331-2d34-3933322d3462';