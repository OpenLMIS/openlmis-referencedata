ALTER TABLE ONLY supported_programs DROP COLUMN id;

ALTER TABLE ONLY supported_programs
  ADD CONSTRAINT supported_programs_pkey PRIMARY KEY (facilityid, programid);
