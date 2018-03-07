DELETE FROM referencedata.role_rights
WHERE rightId IN (SELECT id FROM referencedata.rights WHERE name = 'PRODUCTS_MANAGE');

DELETE FROM referencedata.right_assignments WHERE rightName = 'PRODUCTS_MANAGE';

DELETE FROM referencedata.rights WHERE name = 'PRODUCTS_MANAGE';
