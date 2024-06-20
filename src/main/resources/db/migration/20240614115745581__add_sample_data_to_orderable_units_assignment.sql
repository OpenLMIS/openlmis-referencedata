INSERT INTO orderable_units_assignment (orderableid, orderableversionnumber, unitoforderableid)
    SELECT o.id, o.versionnumber, 'c86e7d33-f8f8-4e0d-b540-89b16ffd71f2' FROM orderables o
WHERE
    NOT EXISTS (
        SELECT * FROM orderable_units_assignment WHERE orderableid = o.id
        AND orderableversionnumber = o.versionnumber
    );
