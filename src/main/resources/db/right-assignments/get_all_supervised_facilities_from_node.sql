-- REPLACED WITH get_all_node_facility_program_mappings.sql
-- This SQL query is necessary in order to conceptually separate out the recursion involved in
-- getting all supervised facilities, direct and indirect, under a supervisory node.
--
-- The recursive CTE supervisory_nodes_recursive returns a list of all supervisory node IDs that 
-- are descendants of the supervisory node ID specified. That list is then fed to the query below
-- to expand each supervisory node ID into a list of facility IDs (based on the specified program
-- ID), then merged into a distinct list.
--
-- Input: supervisory node ID and program ID
-- Output: a list of all supervised facility IDs
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
  JOIN referencedata.supported_programs sp ON sp.facilityid = rgm.facilityid
    AND sp.programid = rgps.programid
    AND sp.active = TRUE
WHERE rgps.programid = ?
;