INSERT INTO referencedata.rights (id, description, name, type)
SELECT '88f87966-9c78-4cc5-a34a-fdd01b3bc42d', NULL, 'REPORTS_MANAGE', 'REPORTS'
WHERE NOT EXISTS (
    SELECT 1 FROM referencedata.rights WHERE name = 'REPORTS_MANAGE'
);

INSERT INTO referencedata.rights (id, description, name, type)
SELECT '353f6664-439e-4e08-93ac-ba853d8f22a8', NULL, 'REPORT_CATEGORIES_MANAGE', 'REPORTS'
WHERE NOT EXISTS (
    SELECT 1 FROM referencedata.rights WHERE name = 'REPORT_CATEGORIES_MANAGE'
);
