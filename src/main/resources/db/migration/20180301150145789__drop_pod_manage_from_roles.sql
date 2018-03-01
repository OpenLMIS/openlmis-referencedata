DELETE FROM referencedata.role_rights
WHERE rightId IN (SELECT id FROM referencedata.rights WHERE name = 'PODS_MANAGE');
