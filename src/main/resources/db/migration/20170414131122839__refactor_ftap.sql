ALTER TABLE facility_type_approved_products
ADD COLUMN orderableid UUID,
ADD COLUMN programid UUID;

UPDATE facility_type_approved_products
SET orderableid = program_orderables.orderableid,
programid = program_orderables.programid
FROM program_orderables
WHERE facility_type_approved_products.programorderableid = program_orderables.id;

ALTER TABLE facility_type_approved_products
DROP COLUMN programorderableid;

ALTER TABLE facility_type_approved_products ALTER COLUMN orderableid SET NOT NULL;
ALTER TABLE facility_type_approved_products ALTER COLUMN programid SET NOT NULL;
