-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

ALTER TABLE facility_type_approved_products
  DROP CONSTRAINT ftaps_orderableid_orderableversionid_fkey;

ALTER TABLE facility_type_approved_products
  DROP COLUMN orderableVersionId;

CREATE OR REPLACE FUNCTION check_ftap_orderable() returns trigger LANGUAGE plpgsql AS $$
DECLARE
orderableId uuid;
BEGIN

  SELECT id
  INTO orderableId
  FROM referencedata.orderables
  WHERE id = NEW.orderableId;

  IF orderableId IS NULL
  THEN
    RAISE 'Orderable with id % does not exist', NEW.orderableId USING ERRCODE = 'foreign_key_violation';
  ELSE
    RETURN NEW;
  END IF;

END $$;

CREATE CONSTRAINT TRIGGER check_ftap_orderable
    AFTER INSERT OR UPDATE ON facility_type_approved_products
    INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_ftap_orderable();
