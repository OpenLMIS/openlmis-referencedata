-- Rights checked by the buq service for population-based vaccine forecasting. Each right is
-- skipped when one with the same name already exists, so the migration is safe to re-run.
INSERT INTO referencedata.rights (id, description, name, type)
SELECT CAST(new_right.id AS UUID), NULL, new_right.name, new_right.type
FROM (VALUES
    ('844bcd9c-e6d5-46f1-a445-527e53694d50', 'VACCINE_FORECAST_PREPARE', 'SUPERVISION'),
    ('4603322e-e0a9-40d9-9952-802399346aee', 'VACCINE_FORECAST_SUBMIT', 'SUPERVISION'),
    ('8441a337-59f6-4560-a7d9-575b8ad24dc1', 'VACCINE_FORECAST_AUTHORIZE', 'SUPERVISION'),
    ('31dce174-b45d-423a-9480-c12a16d03e4e', 'VACCINE_FORECAST_APPROVE', 'SUPERVISION'),
    ('173d0758-b3e4-4068-b8f2-a61e433cc570', 'VACCINE_FORECAST_MANAGE', 'GENERAL_ADMIN')
) AS new_right (id, name, type)
WHERE NOT EXISTS (SELECT 1 FROM referencedata.rights WHERE name = new_right.name);

-- Granted like the other *_MANAGE admin rights. A same-named right of another type is left
-- ungranted: a role's rights must share one type.
INSERT INTO referencedata.role_rights (roleid, rightid)
SELECT CAST('a439c5de-b8aa-11e6-80f5-76304dec7eb7' AS UUID), r.id
FROM referencedata.rights r
WHERE r.name = 'VACCINE_FORECAST_MANAGE'
  AND r.type = 'GENERAL_ADMIN'
  AND EXISTS (
    SELECT 1 FROM referencedata.roles WHERE id = CAST('a439c5de-b8aa-11e6-80f5-76304dec7eb7' AS UUID)
  )
  AND NOT EXISTS (
    SELECT 1
    FROM referencedata.role_rights
    WHERE roleid = CAST('a439c5de-b8aa-11e6-80f5-76304dec7eb7' AS UUID)
      AND rightid = r.id
  );

-- Permission checks read right_assignments, which is otherwise rebuilt only by a full
-- regeneration, so the new grant is added for the users who hold the role directly.
INSERT INTO referencedata.right_assignments (id, rightname, facilityid, programid, userid)
SELECT DISTINCT ON (ra.userid) uuid_generate_v4(), 'VACCINE_FORECAST_MANAGE', NULL, NULL, ra.userid
FROM referencedata.role_assignments ra
WHERE ra.roleid = CAST('a439c5de-b8aa-11e6-80f5-76304dec7eb7' AS UUID)
  AND ra.type = 'direct'
  AND EXISTS (
    SELECT 1
    FROM referencedata.role_rights rr
    JOIN referencedata.rights r ON r.id = rr.rightid
    WHERE rr.roleid = ra.roleid
      AND r.name = 'VACCINE_FORECAST_MANAGE'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM referencedata.right_assignments existing
    WHERE existing.userid = ra.userid
      AND existing.rightname = 'VACCINE_FORECAST_MANAGE'
      AND existing.facilityid IS NULL
      AND existing.programid IS NULL
  );
