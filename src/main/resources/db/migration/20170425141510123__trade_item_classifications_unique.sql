ALTER TABLE ONLY trade_item_classifications
    ADD CONSTRAINT unq_trade_item_classifications_system
    UNIQUE(tradeitemid, classificationsystem);