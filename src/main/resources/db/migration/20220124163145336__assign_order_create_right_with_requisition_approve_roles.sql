INSERT INTO referencedata.role_rights (roleid, rightid)
SELECT roleid, (SELECT id FROM referencedata.rights WHERE name = 'ORDER_CREATE')
FROM referencedata.role_rights
WHERE rightid = (SELECT id FROM referencedata.rights WHERE name = 'REQUISITION_APPROVE');

