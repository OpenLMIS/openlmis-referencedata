-- Schema migrations of new dispensable tables
CREATE TABLE dispensables (
    id uuid NOT NULL PRIMARY KEY
);

CREATE TABLE dispensable_attributes (
    dispensableid uuid NOT NULL,
    key text NOT NULL,
    value text NOT NULL,
    PRIMARY KEY (dispensableid, key),
    FOREIGN KEY (dispensableid) REFERENCES dispensables
);

-- Copy all existing dispensing unit data to new tables.
-- Do not enforce NOT NULL on dispensableid yet.
ALTER TABLE referencedata.orderables ADD COLUMN dispensableid uuid;

WITH new_dispensable_ids_in_orderables AS (
    UPDATE referencedata.orderables
    SET dispensableid = uuid_generate_v4()
    RETURNING dispensableid
)
INSERT INTO referencedata.dispensables
SELECT dispensableid
FROM new_dispensable_ids_in_orderables
;

INSERT INTO referencedata.dispensable_attributes
SELECT dispensableid, 'dispensingUnit', dispensingunit
FROM referencedata.orderables o
;

-- Now that existing data has been migrated, enforce NOT NULL on dispensableid.
-- Also, add other constraints and drop old column.
ALTER TABLE referencedata.orderables ALTER COLUMN dispensableid SET NOT NULL;
ALTER TABLE referencedata.orderables ADD CONSTRAINT orderables_dispensableid_fkey FOREIGN KEY (dispensableid) REFERENCES dispensables;
ALTER TABLE referencedata.orderables DROP COLUMN dispensingunit;