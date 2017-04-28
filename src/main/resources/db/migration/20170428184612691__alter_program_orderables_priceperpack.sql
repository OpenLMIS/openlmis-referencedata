ALTER TABLE program_orderables
  RENAME COLUMN priceperpack TO priceperpackamount;
ALTER TABLE program_orderables
  ADD COLUMN priceperpackcurrency character varying(3);
