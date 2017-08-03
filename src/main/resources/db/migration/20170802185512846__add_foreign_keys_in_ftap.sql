ALTER TABLE ONLY facility_type_approved_products
    ADD CONSTRAINT ftap_orderableid_fk FOREIGN KEY (orderableid) REFERENCES orderables(id);


ALTER TABLE ONLY facility_type_approved_products
    ADD CONSTRAINT ftap_programid_fk FOREIGN KEY (programid) REFERENCES programs(id);
