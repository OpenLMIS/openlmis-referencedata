WITH existing_right AS (
    SELECT id, name
    FROM referencedata.rights
    WHERE name IN ('REPORTS_MANAGE', 'REPORT_CATEGORIES_MANAGE')
),
inserted_right AS (
    INSERT INTO referencedata.rights (id, description, name, type)
    SELECT CAST('88f87966-9c78-4cc5-a34a-fdd01b3bc42d' AS UUID), NULL, 'REPORTS_MANAGE', 'GENERAL_ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM existing_right WHERE name = 'REPORTS_MANAGE')
    UNION ALL
    SELECT CAST('353f6664-439e-4e08-93ac-ba853d8f22a8' AS UUID), NULL, 'REPORT_CATEGORIES_MANAGE', 'GENERAL_ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM existing_right WHERE name = 'REPORT_CATEGORIES_MANAGE')
    RETURNING id, name
),
all_rights AS (
    SELECT * FROM existing_right
    UNION ALL
    SELECT * FROM inserted_right
)
INSERT INTO referencedata.role_rights (roleid, rightid)
SELECT CAST('a439c5de-b8aa-11e6-80f5-76304dec7eb7' AS UUID), id
FROM all_rights
WHERE NOT EXISTS (
    SELECT 1
    FROM referencedata.role_rights
    WHERE roleid = CAST('a439c5de-b8aa-11e6-80f5-76304dec7eb7' AS UUID)
      AND rightid = all_rights.id
);
