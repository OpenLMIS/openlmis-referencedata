INSERT INTO referencedata.users (id, username, firstName, lastName, email, timezone) VALUES ('35316636-6264-6331-2d34-3933322d3462', 'admin', 'Admin', 'User', 'example@mail.com', 'UTC');

INSERT INTO referencedata.rights (id, name, type) VALUES ('4e731cf7-854f-4af7-9ea4-bd5d8ed7bb22', 'MANAGE_GEOGRAPHIC_ZONE', 'GENERAL_ADMIN');
INSERT INTO referencedata.rights (id, name, type) VALUES ('5c4b3b9b-713e-4b9a-8c58-7efcd2954512', 'MANAGE_SUPERVISORY_NODE', 'GENERAL_ADMIN');
INSERT INTO referencedata.rights (id, name, type) VALUES ('fb6a0053-6254-4b41-8028-bf91421f90dd', 'MANAGE_PRODUCT', 'GENERAL_ADMIN');
INSERT INTO referencedata.rights (id, name, type) VALUES ('9ade922b-3523-4582-bef4-a47701f7df14', 'REQUISITION_CREATE', 'SUPERVISION');
INSERT INTO referencedata.rights (id, name, type) VALUES ('bffa2de2-dc2a-47dd-b126-6501748ac3fc', 'REQUISITION_APPROVE', 'SUPERVISION');
INSERT INTO referencedata.rights (id, name, type) VALUES ('feb4c0b8-f6d2-4289-b29d-811c1d0b2863', 'REQUISITION_AUTHORIZE', 'SUPERVISION');
INSERT INTO referencedata.rights (id, name, type) VALUES ('c3eb5df0-c3ac-4e70-a978-02827462f60e', 'REQUISITION_DELETE', 'SUPERVISION');
INSERT INTO referencedata.rights (id, name, type) VALUES ('e101d2b8-6a0f-4af6-a5de-a9576b4ebc50', 'REQUISITION_VIEW', 'SUPERVISION');
