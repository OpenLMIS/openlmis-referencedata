INSERT INTO facility_types (id, code, name, active, displayOrder)
    VALUES ('e2faaa9e-4b2d-4212-bb60-fd62970b2113', 'warehouse', 'Warehouse', true, 1)
        ON CONFLICT (code) DO NOTHING;