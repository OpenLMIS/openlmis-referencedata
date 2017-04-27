ALTER TABLE ONLY facility_type_approved_products
    ADD CONSTRAINT unq_ftap
    UNIQUE(facilitytypeid, orderableid, programid);