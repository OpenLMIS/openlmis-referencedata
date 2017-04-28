ALTER TABLE orderables
    DROP CONSTRAINT orderables_parent,
    DROP COLUMN type,
    DROP COLUMN classificationsystem,
    DROP COLUMN classificationid,
    DROP COLUMN parentid,
    ADD COLUMN commoditytypeid uuid,
    ADD COLUMN tradeitemid uuid;

CREATE TABLE identifiers (
    key character varying(255) NOT NULL,
    value character varying(255) NOT NULL,
    orderableId uuid NOT NULL,
    UNIQUE (key, orderableId),
    FOREIGN KEY (orderableId) REFERENCES orderables(id)
);

CREATE TABLE commodity_types (
    id uuid PRIMARY KEY,
    name character varying(255),
    classificationsystem character varying(255),
    classificationid character varying(255),
    parentid uuid
);

CREATE TABLE trade_items (
    id uuid PRIMARY KEY,
    manufacturerOfTradeItem character varying(255)
);

ALTER TABLE ONLY orderables
    ADD CONSTRAINT orderables_commodity_type_fkey FOREIGN KEY (commoditytypeid)
    REFERENCES commodity_types(id);

ALTER TABLE ONLY orderables
    ADD CONSTRAINT orderables_trade_item_fkey FOREIGN KEY (tradeitemid)
    REFERENCES trade_items(id);

ALTER TABLE ONLY commodity_types
    ADD CONSTRAINT commodity_types_parent FOREIGN KEY (parentid)
    REFERENCES commodity_types(id);

ALTER TABLE trade_item_classifications
    DROP CONSTRAINT trade_item_classifications_orderables;

ALTER TABLE ONLY trade_item_classifications
    ADD CONSTRAINT trade_item_classifications_trade_items FOREIGN KEY (tradeitemid)
    REFERENCES trade_items(id);
