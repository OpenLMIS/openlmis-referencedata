ALTER TABLE ONLY ideal_stock_amounts
    DROP CONSTRAINT isa_programid_fk;

ALTER TABLE ONLY ideal_stock_amounts
    DROP CONSTRAINT isa_orderableid_fk;

ALTER TABLE ONLY ideal_stock_amounts
    DROP COLUMN programid;

ALTER TABLE ONLY ideal_stock_amounts
    DROP COLUMN orderableid;

ALTER TABLE ONLY ideal_stock_amounts
    ADD COLUMN commoditytypeid uuid NOT NULL;

ALTER TABLE ONLY ideal_stock_amounts
    ADD CONSTRAINT isa_commoditytypeid_fk FOREIGN KEY (commoditytypeid) REFERENCES commodity_types(id);