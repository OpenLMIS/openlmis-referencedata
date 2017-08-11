ALTER TABLE lots ADD PRIMARY KEY (id);

ALTER TABLE lots ADD FOREIGN KEY (tradeitemid) REFERENCES trade_items(id);
