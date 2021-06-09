ALTER TABLE facility_types
    DROP CONSTRAINT facility_types_uk_name;

CREATE UNIQUE INDEX unq_facility_type_name ON facility_types (LOWER(name));