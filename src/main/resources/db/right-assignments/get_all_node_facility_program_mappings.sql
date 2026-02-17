WITH RECURSIVE hierarchy(parent, child) AS (
    -- Base case: Every node is its own parent
    SELECT id, id FROM referencedata.supervisory_nodes
    UNION ALL
    -- Recursive step: Link parent supervisory node to all children/grandchildren nodes
    SELECT h.parent, n.id
    FROM hierarchy h
             JOIN referencedata.supervisory_nodes n ON n.parentid = h.child
)
SELECT DISTINCT
    h.parent AS supervisorynodeid,
    pg.programid AS programid,
    fm.facilityid AS facilityid
FROM hierarchy h
         JOIN referencedata.requisition_groups rg ON rg.supervisorynodeid = h.child
         JOIN referencedata.requisition_group_members fm ON fm.requisitiongroupid = rg.id
         JOIN referencedata.requisition_group_program_schedules pg ON pg.requisitiongroupid = rg.id
         JOIN referencedata.supported_programs sp
              ON sp.facilityid = fm.facilityid
                  AND sp.programid = pg.programid
                  AND sp.active = TRUE;
