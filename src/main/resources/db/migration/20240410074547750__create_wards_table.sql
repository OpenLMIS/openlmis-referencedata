CREATE TABLE wards (
    id UUID PRIMARY KEY,
    facilityid UUID NOT NULL,
    name text,
    description text,
    code text NOT NULL,
    disabled boolean NOT NULL,
    CONSTRAINT ward_facility_fk FOREIGN KEY (facilityid) REFERENCES facilities(id),
    CONSTRAINT unq_ward_code UNIQUE (code)
);