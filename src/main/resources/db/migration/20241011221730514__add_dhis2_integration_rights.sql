INSERT INTO referencedata.rights (id, description, name, type) VALUES ('db1f5075-6894-4b2d-a4b2-efc4652f09d5', NULL, 'MANAGE_DHIS2_SUPERVISORY_NODES', 'SUPERVISION');
INSERT INTO referencedata.rights (id, description, name, type) VALUES ('85abf93a-ea22-42a9-be7c-5baf64d757f9', NULL, 'MANAGE_DHIS2', 'GENERAL_ADMIN');
INSERT INTO referencedata.rights (id, description, name, type) VALUES ('f37f5982-e36d-4712-9b7f-01c703df5fb5', NULL, 'MANAGE_DHIS2_PERIODS', 'GENERAL_ADMIN');

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'db1f5075-6894-4b2d-a4b2-efc4652f09d5');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '85abf93a-ea22-42a9-be7c-5baf64d757f9');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'f37f5982-e36d-4712-9b7f-01c703df5fb5');