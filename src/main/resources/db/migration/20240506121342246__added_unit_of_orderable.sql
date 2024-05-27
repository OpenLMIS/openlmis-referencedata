CREATE TABLE unit_of_orderables
(
    id UUID PRIMARY KEY,
    name         TEXT,
    description  TEXT,
    displayOrder INTEGER NOT NULL,
    factor       INTEGER NOT NULL
);

CREATE TABLE orderable_units_assignment
(
    orderableId UUID,
    orderableVersionNumber BIGINT,
    unitoforderableid UUID,
    CONSTRAINT fk_orderable FOREIGN KEY (orderableId, orderableVersionNumber) REFERENCES orderables (id, versionNumber),
    CONSTRAINT fk_unit_of_orderable FOREIGN KEY (unitoforderableid) REFERENCES unit_of_orderables (id)
);
