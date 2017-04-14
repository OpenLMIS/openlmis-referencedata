ALTER TABLE facility_type_approved_products
DROP COLUMN programorderableid,
ADD COLUMN orderableid UUID NOT NULL,
ADD COLUMN programid UUID NOT NULL;