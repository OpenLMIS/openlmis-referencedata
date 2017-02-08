ALTER TABLE facility_type_approved_products
  RENAME COLUMN maxmonthsofstock TO maxperiodsofstock;
ALTER TABLE facility_type_approved_products
  RENAME COLUMN minmonthsofstock TO minperiodsofstock;
ALTER TABLE program_orderables
  RENAME COLUMN dosespermonth TO dosesperpatient;
ALTER TABLE program_orderables
  DROP COLUMN maxmonthsstock;
