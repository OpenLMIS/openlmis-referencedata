-- Add new column
ALTER TABLE orderable_children ADD COLUMN unitId UUID;
-- Migrate existing records to Singe Dose
UPDATE orderable_children
SET unitId = 'c86e7d33-f8f8-4e0d-b540-89b16ffd71f2'
WHERE unitId is null;
-- Make column required
ALTER TABLE orderable_children ALTER COLUMN unitId SET NOT NULL;

-- Update unique constraint of OrderableChildren
ALTER TABLE orderable_children
    DROP CONSTRAINT unq_orderable_parent_id;
ALTER TABLE orderable_children
    ADD CONSTRAINT unq_orderable_parent_id
        UNIQUE (orderableid, orderableVersionNumber, parentid, parentVersionNumber, unitId);
