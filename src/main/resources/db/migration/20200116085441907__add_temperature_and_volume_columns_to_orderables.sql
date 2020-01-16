-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

ALTER TABLE referencedata.orderables ADD COLUMN minimumToleranceTemperatureValue double precision;

ALTER TABLE referencedata.orderables ADD COLUMN minimumToleranceTemperatureCode character varying(30);

ALTER TABLE referencedata.orderables ADD COLUMN maximumToleranceTemperatureValue double precision;

ALTER TABLE referencedata.orderables ADD COLUMN maximumToleranceTemperatureCode character varying(30);

ALTER TABLE referencedata.orderables ADD COLUMN inBoxCubeDimensionValue double precision;

ALTER TABLE referencedata.orderables ADD COLUMN inBoxCubeDimensionCode character varying(30);