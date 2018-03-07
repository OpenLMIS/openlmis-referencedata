ALTER TABLE facility_types
    DROP CONSTRAINT uk_nfppl8ui0vgjoxenm5v2727wo;

CREATE UNIQUE INDEX unq_facility_type_code ON facility_types (LOWER(code));