-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

ALTER TABLE orderables
  RENAME COLUMN versionid TO versionnumber;

ALTER TABLE referencedata.facility_type_approved_products
  RENAME COLUMN versionid TO versionnumber;

ALTER TABLE referencedata.supply_partner_association_orderables
  RENAME COLUMN orderableversionid TO orderableversionnumber;

ALTER TABLE referencedata.orderable_identifiers
  RENAME COLUMN orderableversionid TO orderableversionnumber;

ALTER TABLE referencedata.program_orderables
  RENAME COLUMN orderableversionid TO orderableversionnumber;

ALTER TABLE orderable_children
  RENAME COLUMN orderableVersionId TO orderableversionnumber;

ALTER TABLE orderable_children
  RENAME COLUMN parentVersionId TO parentVersionNumber;

ALTER INDEX unq_programid_orderableid_orderableversionid
  RENAME TO unq_programid_orderableid_orderableversionnumber;