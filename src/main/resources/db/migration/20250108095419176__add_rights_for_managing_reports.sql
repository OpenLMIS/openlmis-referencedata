INSERT INTO referencedata.rights (id, description, name, type)
SELECT '88f87966-9c78-4cc5-a34a-fdd01b3bc42d', NULL, 'REPORTS_MANAGE', 'GENERAL_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM referencedata.rights WHERE name = 'REPORTS_MANAGE'
);

INSERT INTO referencedata.rights (id, description, name, type)
SELECT '353f6664-439e-4e08-93ac-ba853d8f22a8', NULL, 'REPORT_CATEGORIES_MANAGE', 'GENERAL_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM referencedata.rights WHERE name = 'REPORT_CATEGORIES_MANAGE'
);

INSERT INTO referencedata.role_rights (roleid, rightid)
SELECT 'a439c5de-b8aa-11e6-80f5-76304dec7eb7', '88f87966-9c78-4cc5-a34a-fdd01b3bc42d'
WHERE NOT EXISTS (
    SELECT 1
    FROM referencedata.role_rights
    WHERE roleid = 'a439c5de-b8aa-11e6-80f5-76304dec7eb7'
      AND rightid = '88f87966-9c78-4cc5-a34a-fdd01b3bc42d'
);

INSERT INTO referencedata.role_rights (roleid, rightid)
SELECT 'a439c5de-b8aa-11e6-80f5-76304dec7eb7', '353f6664-439e-4e08-93ac-ba853d8f22a8'
WHERE NOT EXISTS (
    SELECT 1
    FROM referencedata.role_rights
    WHERE roleid = 'a439c5de-b8aa-11e6-80f5-76304dec7eb7'
      AND rightid = '353f6664-439e-4e08-93ac-ba853d8f22a8'
);
