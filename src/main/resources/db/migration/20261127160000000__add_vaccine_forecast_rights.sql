-- Rights checked by the buq service for population-based vaccine forecasting. A right that already
-- exists by name is kept.
INSERT INTO referencedata.rights (id, description, name, type)
SELECT CAST(new_right.id AS UUID), NULL, new_right.name, new_right.type
FROM (
    SELECT supervision_right.id, supervision_right.name, 'SUPERVISION' AS type
    FROM (VALUES
        ('844bcd9c-e6d5-46f1-a445-527e53694d50', 'VACCINE_FORECAST_PREPARE'),
        ('4603322e-e0a9-40d9-9952-802399346aee', 'VACCINE_FORECAST_SUBMIT'),
        ('8441a337-59f6-4560-a7d9-575b8ad24dc1', 'VACCINE_FORECAST_AUTHORIZE'),
        ('31dce174-b45d-423a-9480-c12a16d03e4e', 'VACCINE_FORECAST_APPROVE')
    ) AS supervision_right (id, name)
    UNION ALL
    SELECT '173d0758-b3e4-4068-b8f2-a61e433cc570', 'VACCINE_FORECAST_MANAGE', 'GENERAL_ADMIN'
) AS new_right
LEFT JOIN referencedata.rights existing ON existing.name = new_right.name
WHERE existing.id IS NULL;

-- System Administrator gets the manage right, like the other *_MANAGE admin rights. A same-named
-- right of another type is left ungranted: a role's rights must share one type. Permission checks
-- read right_assignments, which is otherwise rebuilt only by a full regeneration, so the users
-- holding the role directly get the right there too.
WITH manage AS (
    SELECT r.id AS right_id, r.name, ro.id AS role_id
    FROM referencedata.rights r
    JOIN referencedata.roles ro ON ro.id = CAST('a439c5de-b8aa-11e6-80f5-76304dec7eb7' AS UUID)
    WHERE r.name = 'VACCINE_FORECAST_MANAGE'
      AND r.type = 'GENERAL_ADMIN'
),
granted AS (
    INSERT INTO referencedata.role_rights (roleid, rightid)
    SELECT manage.role_id, manage.right_id
    FROM manage
    LEFT JOIN referencedata.role_rights existing
        ON existing.roleid = manage.role_id AND existing.rightid = manage.right_id
    WHERE existing.roleid IS NULL
    RETURNING roleid
)
INSERT INTO referencedata.right_assignments (id, rightname, facilityid, programid, userid)
SELECT DISTINCT ON (ra.userid) uuid_generate_v4(), manage.name, NULL, NULL, ra.userid
FROM manage
JOIN referencedata.role_assignments ra ON ra.roleid = manage.role_id AND ra.type = 'direct'
LEFT JOIN referencedata.right_assignments existing
    ON existing.userid = ra.userid
    AND existing.rightname = manage.name
    AND existing.facilityid IS NULL
    AND existing.programid IS NULL
WHERE existing.id IS NULL;
