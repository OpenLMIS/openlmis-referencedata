-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE TABLE new_facility_type_approved_products (
    id uuid NOT NULL,
    versionid bigint NOT NULL,
    orderableId UUID NOT NULL,
    programId UUID NOT NULL,
    facilityTypeId uuid NOT NULL,
    maxPeriodsOfStock double precision NOT NULL,
    minPeriodsOfStock double precision,
    emergencyOrderPoint double precision,
    active boolean DEFAULT true NOT NULL,
    lastupdated timestamptz DEFAULT now() NOT NULL
);

INSERT INTO new_facility_type_approved_products(id, versionid, orderableId, programId, facilityTypeId, maxPeriodsOfStock, minPeriodsOfStock, emergencyOrderPoint, active, lastupdated)
  SELECT id, 1, orderableId, programId, facilityTypeId, maxPeriodsOfStock, minPeriodsOfStock, emergencyOrderPoint, active, NOW()
  FROM facility_type_approved_products;

DROP TABLE facility_type_approved_products;
ALTER TABLE new_facility_type_approved_products
  RENAME TO facility_type_approved_products;

ALTER TABLE ONLY facility_type_approved_products
  ADD CONSTRAINT facility_type_approved_products_pkey PRIMARY KEY (id, versionid),
  ADD CONSTRAINT ftap_programid_fk FOREIGN KEY (programId) REFERENCES programs(id),
  ADD CONSTRAINT ftap_facilitytypeid_fk FOREIGN KEY (facilityTypeId) REFERENCES facility_types(id);

CREATE UNIQUE INDEX unq_ftap
  ON facility_type_approved_products (facilitytypeid, orderableid, programid)
  WHERE active IS TRUE;

CREATE INDEX ON referencedata.facility_type_approved_products (orderableId);
CREATE INDEX ON referencedata.facility_type_approved_products (programId);
CREATE INDEX ON referencedata.facility_type_approved_products (facilityTypeId);

CREATE CONSTRAINT TRIGGER check_ftap_orderable
    AFTER INSERT OR UPDATE ON facility_type_approved_products
    INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE PROCEDURE check_ftap_orderable();
