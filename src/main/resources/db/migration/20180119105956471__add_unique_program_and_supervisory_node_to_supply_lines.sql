ALTER TABLE ONLY supply_lines
    ADD CONSTRAINT supply_line_unique_program_supervisory_node UNIQUE (supervisorynodeid, programid);