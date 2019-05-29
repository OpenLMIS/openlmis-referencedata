-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

INSERT INTO rights (id, description, name, type) VALUES ('75773629-f848-480f-83d5-973673ade29c', NULL, 'ADMINISTRATIVE_MESSAGES_MANAGE', 'GENERAL_ADMIN');

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '75773629-f848-480f-83d5-973673ade29c');
