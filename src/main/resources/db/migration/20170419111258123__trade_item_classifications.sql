ALTER TABLE orderables DROP COLUMN commoditytypeid;
ALTER TABLE orderables ADD COLUMN classificationsystem character varying(255);
ALTER TABLE orderables ADD COLUMN classificationid character varying(255);
ALTER TABLE orderables ADD COLUMN parentid uuid;

ALTER TABLE ONLY orderables
    ADD CONSTRAINT orderables_parent FOREIGN KEY (parentid)
    REFERENCES orderables(id);

CREATE TABLE trade_item_classifications (
    id uuid NOT NULL,
    classificationsystem character varying(255) NOT NULL,
    classificationid character varying(255) NOT NULL,
    tradeitemid uuid NOT NULL
);

ALTER TABLE ONLY trade_item_classifications
    ADD CONSTRAINT trade_item_classifications_orderables FOREIGN KEY (tradeitemid)
    REFERENCES orderables(id);