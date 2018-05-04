ALTER TABLE ONLY program_orderables
    ADD CONSTRAINT unq_orderableid_programid UNIQUE (orderableid, programid);
