SELECT DISTINCT ra.userid
  , ri.name AS rightname
  , CASE WHEN ra.programid IS NOT NULL THEN u.homefacilityid
         WHEN ra.warehouseid IS NOT NULL THEN ra.warehouseid
         ELSE NULL
    END AS facilityid
  , ra.programid
  , ra.supervisorynodeid
FROM referencedata.role_assignments ra
  JOIN referencedata.roles ro ON ro.id = ra.roleid
  JOIN referencedata.role_rights rr ON rr.roleid = ro.id
  JOIN referencedata.rights ri ON ri.id = rr.rightid
  JOIN referencedata.users u ON u.id = ra.userid
;