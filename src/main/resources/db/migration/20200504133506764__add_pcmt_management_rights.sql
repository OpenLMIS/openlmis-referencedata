-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

INSERT INTO referencedata.rights (id, description, name, type) VALUES ('5920d0eb-c2df-411f-9d16-bf1e9b745bd9', NULL, 'PCMT_MANAGEMENT', 'GENERAL_ADMIN');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '5920d0eb-c2df-411f-9d16-bf1e9b745bd9');