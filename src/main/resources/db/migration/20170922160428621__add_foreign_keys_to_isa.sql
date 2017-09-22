ALTER TABLE ONLY ideal_stock_amounts
    ADD CONSTRAINT isa_facilityid_fk FOREIGN KEY (facilityid) REFERENCES facilities(id);

ALTER TABLE ONLY ideal_stock_amounts
    ADD CONSTRAINT isa_programid_fk FOREIGN KEY (programid) REFERENCES programs(id);

ALTER TABLE ONLY ideal_stock_amounts
    ADD CONSTRAINT isa_orderableid_fk FOREIGN KEY (orderableid) REFERENCES orderables(id);

ALTER TABLE ONLY ideal_stock_amounts
    ADD CONSTRAINT isa_processingperiodid_fk FOREIGN KEY (processingperiodid) REFERENCES processing_periods(id);