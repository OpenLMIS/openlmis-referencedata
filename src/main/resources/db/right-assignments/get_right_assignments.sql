-- This SQL takes global role assignments (just role ID), fulfillment role assignments (with 
-- role ID and warehouse ID) and home facility supervision role assignments (with role ID and 
-- program ID but not supervisory node ID) and turns each into a set of right assignments, 
-- expanding a role into its rights. Facility ID is determined by: if it has a program ID, it is 
-- a home facility supervision role assignment, so use user's home facility ID; if it has a 
-- warehouse ID, it is a fulfillment role assignment, so use the warehouse ID itself; else use 
-- NULL.
-- 
-- The CTE filtered_role_assignments is used to filter out all home facility supervision role 
-- assignments where the home facility does not support the program, even though there is a home 
-- facility supervision role assignment of that facility-program combo.
WITH filtered_role_assignments AS
(
  SELECT ra.*
  FROM referencedata.role_assignments ra
  EXCEPT
  SELECT ra.*
  FROM referencedata.role_assignments ra
    INNER JOIN referencedata.users u ON ra.userid = u.id
    LEFT JOIN referencedata.supported_programs sp ON sp.facilityid = u.homefacilityid
      AND sp.programid = ra.programid
  WHERE ra.type = 'supervision'
    AND ra.programid IS NOT NULL
    AND ra.supervisorynodeid IS NULL
    AND (sp.active = FALSE OR sp.active IS NULL)
)
SELECT DISTINCT ra.userid
  , ri.name AS rightname
  , CASE WHEN ra.programid IS NOT NULL THEN u.homefacilityid
         WHEN ra.warehouseid IS NOT NULL THEN ra.warehouseid
         ELSE NULL
    END AS facilityid
  , ra.programid
  , ra.supervisorynodeid
FROM filtered_role_assignments ra
  INNER JOIN referencedata.role_rights rr ON rr.roleid = ra.roleid
  INNER JOIN referencedata.rights ri ON ri.id = rr.rightid
  INNER JOIN referencedata.users u ON u.id = ra.userid
;