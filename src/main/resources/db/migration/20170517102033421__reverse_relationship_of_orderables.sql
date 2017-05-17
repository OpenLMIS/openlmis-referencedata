CREATE TABLE commodity_types (
    id uuid PRIMARY KEY,
    name character varying(255) NOT NULL,
    classificationsystem character varying(255) NOT NULL,
    classificationid character varying(255) NOT NULL,
    parentid uuid
);

INSERT INTO commodity_types (id, name, classificationsystem, classificationid, parentid)
SELECT id, fullproductname, classificationsystem, classificationid, parentid
FROM orderables
WHERE type='COMMODITY_TYPE'
    AND fullproductname IS NOT NULL
    AND classificationsystem IS NOT NULL
    AND classificationid IS NOT NULL;

CREATE TABLE trade_items (
    id uuid PRIMARY KEY,
    manufacturerOfTradeItem character varying(255) NOT NULL
);

INSERT INTO trade_items (id, manufacturerOfTradeItem)
SELECT id, manufacturerOfTradeItem
FROM orderables
WHERE type='TRADE_ITEM' AND
    manufacturerOfTradeItem IS NOT NULL;

CREATE TABLE identifiers (
    key character varying(255) NOT NULL,
    value character varying(255) NOT NULL,
    orderableId uuid NOT NULL,
    UNIQUE (key, orderableId),
    FOREIGN KEY (orderableId) REFERENCES orderables(id)
);

INSERT INTO identifiers(key, value, orderableId)
SELECT 'CommodityType', CAST(id AS varchar(255)), id
FROM orderables
WHERE type='COMMODITY_TYPE'
    AND fullproductname IS NOT NULL
    AND classificationsystem IS NOT NULL
    AND classificationid IS NOT NULL;

INSERT INTO identifiers(key, value, orderableId)
SELECT 'TradeItem', CAST(id AS varchar(255)), id
FROM orderables
WHERE type='TRADE_ITEM' AND
    manufacturerOfTradeItem IS NOT NULL;

ALTER TABLE orderables
    DROP CONSTRAINT orderables_parent,
    DROP COLUMN type,
    DROP COLUMN classificationsystem,
    DROP COLUMN classificationid,
    DROP COLUMN manufacturerOfTradeItem,
    DROP COLUMN parentid;

ALTER TABLE ONLY commodity_types
    ADD CONSTRAINT commodity_types_parent FOREIGN KEY (parentid)
    REFERENCES commodity_types(id);

ALTER TABLE trade_item_classifications
    DROP CONSTRAINT trade_item_classifications_orderables;

ALTER TABLE ONLY trade_item_classifications
    ADD CONSTRAINT trade_item_classifications_trade_items FOREIGN KEY (tradeitemid)
    REFERENCES trade_items(id);
