ALTER TABLE ONLY facilities DROP CONSTRAINT uk_mnsci7u7h2r2b3tohhn0b819;
CREATE UNIQUE INDEX unq_facility_code ON facilities (LOWER(code));
