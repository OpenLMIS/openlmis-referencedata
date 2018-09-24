-- Add new columns but do not enforce constraints yet
ALTER TABLE referencedata.orderables ADD COLUMN versionid bigint;
ALTER TABLE referencedata.orderables ADD COLUMN lastupdated timestamptz;
ALTER TABLE referencedata.orderable_identifiers ADD COLUMN orderableversionid bigint;
ALTER TABLE referencedata.program_orderables ADD COLUMN orderableversionid bigint;
ALTER TABLE referencedata.facility_type_approved_products ADD COLUMN orderableversionid bigint;

-- Drop foreign keys pointing to orderables primary key
ALTER TABLE referencedata.orderable_identifiers DROP CONSTRAINT orderable_identifiers_orderableid_fkey;
ALTER TABLE referencedata.program_orderables DROP CONSTRAINT fkp2b6lcwnyqul4yi2vnd2vvq50;
ALTER TABLE referencedata.facility_type_approved_products DROP CONSTRAINT ftap_orderableid_fk;

-- Switch orderables primary key to id and versionid
ALTER TABLE referencedata.orderables DROP CONSTRAINT orderables_pkey;
ALTER TABLE referencedata.orderables ADD CONSTRAINT orderables_pkey PRIMARY KEY (id, versionid);

-- Generate starting values for version ids and last updated
UPDATE referencedata.orderables SET versionid = 1, lastupdated = now();
UPDATE referencedata.orderable_identifiers SET orderableversionid = 1;
UPDATE referencedata.program_orderables SET orderableversionid = 1;
UPDATE referencedata.facility_type_approved_products SET orderableversionid = 1;

-- Add multi-column foreign keys pointing to orderables multi-column primary key
ALTER TABLE referencedata.orderable_identifiers
  ADD CONSTRAINT orderable_identifiers_orderableid_orderableversionid_fkey FOREIGN KEY (orderableid, orderableversionid) REFERENCES orderables(id, versionid);
ALTER TABLE referencedata.program_orderables
  ADD CONSTRAINT program_orderables_orderableid_orderableversionid_fkey FOREIGN KEY (orderableid, orderableversionid) REFERENCES orderables(id, versionid);
ALTER TABLE referencedata.facility_type_approved_products
  ADD CONSTRAINT ftaps_orderableid_orderableversionid_fkey FOREIGN KEY (orderableid, orderableversionid) REFERENCES orderables(id, versionid);

-- Enforce NOT NULL for new columns
ALTER TABLE referencedata.orderables ALTER COLUMN versionid SET NOT NULL;
ALTER TABLE referencedata.orderables ALTER COLUMN lastupdated SET DEFAULT now();
ALTER TABLE referencedata.orderables ALTER COLUMN lastupdated SET NOT NULL;
ALTER TABLE referencedata.orderable_identifiers ALTER COLUMN orderableversionid SET NOT NULL;
ALTER TABLE referencedata.program_orderables ALTER COLUMN orderableversionid SET NOT NULL;
ALTER TABLE referencedata.facility_type_approved_products ALTER COLUMN orderableversionid SET NOT NULL;

-- Recreate unique index for program orderables to include orderable version id
CREATE UNIQUE INDEX unq_programid_orderableid_orderableversionid
  ON referencedata.program_orderables(programid, orderableid, orderableversionid)
  WHERE (active = TRUE)
;
DROP INDEX unq_orderableid_programid;

-- Recreate unique constraint for orderable identifiers to include orderable version id
ALTER TABLE referencedata.orderable_identifiers
  ADD CONSTRAINT unq_orderableid_orderableversionid_key UNIQUE (orderableid, orderableversionid, key);
ALTER TABLE referencedata.orderable_identifiers
  DROP CONSTRAINT orderable_identifiers_key_orderableid_key;

-- Drop unique constraint for product code
ALTER TABLE referencedata.orderables ADD CONSTRAINT unq_productcode_versionid UNIQUE(code, versionid);
ALTER TABLE referencedata.orderables DROP CONSTRAINT unq_productcode;