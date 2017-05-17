INSERT INTO facility_types (id, code, name, active, displayOrder)
    SELECT 'e2faaa9e-4b2d-4212-bb60-fd62970b2113', 'warehouse', 'Warehouse', true, 1
WHERE
    NOT EXISTS (
        SELECT code FROM facility_types WHERE code = 'warehouse'
    );