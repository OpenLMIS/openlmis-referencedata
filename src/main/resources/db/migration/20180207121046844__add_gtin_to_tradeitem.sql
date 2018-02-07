ALTER TABLE referencedata.trade_items ADD COLUMN gtin text;

ALTER TABLE ONLY referencedata.trade_items
    ADD CONSTRAINT uk_tradeitems_gtin UNIQUE (gtin);
