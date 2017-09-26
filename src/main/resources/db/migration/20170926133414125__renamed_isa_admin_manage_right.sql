DELETE FROM role_rights
    WHERE roleid = 'a439c5de-b8aa-11e6-80f5-76304dec7eb7' AND rightid = '3f4c6b74-5426-4646-925b-c1982648846c';
DELETE FROM rights
    WHERE id = '3f4c6b74-5426-4646-925b-c1982648846c';

INSERT INTO rights (id, description, name, type) VALUES ('bc4111a2-14f2-42aa-8f97-d9502d611ac7', NULL, 'SYSTEM_IDEAL_STOCK_AMOUNTS_MANAGE', 'GENERAL_ADMIN');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'bc4111a2-14f2-42aa-8f97-d9502d611ac7');