WITH RECURSIVE supervisory_nodes_recursive AS
(
  SELECT sn.id, rgm.facilityid
  FROM referencedata.supervisory_nodes sn
    JOIN referencedata.requisition_groups rg ON rg.supervisorynodeid = sn.id
    JOIN referencedata.requisition_group_program_schedules rgps ON rgps.requisitiongroupid = rg.id
    JOIN referencedata.requisition_group_members rgm ON rgm.requisitiongroupid = rg.id
  WHERE rg.supervisorynodeid = ?
    AND rgps.programid = ?
  UNION
  SELECT sn.id, rgm.facilityid
  FROM referencedata.supervisory_nodes sn
    JOIN supervisory_nodes_recursive ON sn.parentid = supervisory_nodes_recursive.id
    JOIN referencedata.requisition_groups rg ON rg.supervisorynodeid = sn.id
    JOIN referencedata.requisition_group_program_schedules rgps ON rgps.requisitiongroupid = rg.id
    JOIN referencedata.requisition_group_members rgm ON rgm.requisitiongroupid = rg.id
)
SELECT facilityid FROM supervisory_nodes_recursive;