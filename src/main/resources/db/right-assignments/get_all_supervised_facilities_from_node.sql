WITH RECURSIVE supervisory_nodes_recursive AS
(
  SELECT sn.id
  FROM referencedata.supervisory_nodes sn
  WHERE sn.id = ?
  UNION ALL
  SELECT sn.id
  FROM supervisory_nodes_recursive
  JOIN referencedata.supervisory_nodes sn ON supervisory_nodes_recursive.id = sn.parentid
)
SELECT DISTINCT rgm.facilityid
FROM supervisory_nodes_recursive
  JOIN referencedata.requisition_groups rg ON rg.supervisorynodeid = supervisory_nodes_recursive.id
  JOIN referencedata.requisition_group_members rgm ON rgm.requisitiongroupid = rg.id
  JOIN referencedata.requisition_group_program_schedules rgps ON rgps.requisitiongroupid = rg.id
  JOIN referencedata.supported_programs sp ON sp.facilityid = rgm.facilityid AND sp.programid = rgps.programid
WHERE rgps.programid = ?
;