CREATE TABLE permission_strings (
    id uuid NOT NULL PRIMARY KEY,
    roleassignmentid uuid NOT NULL REFERENCES role_assignments,
    value text NOT NULL
);

-- Bootstrap data for administrator user
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('b4260327-276f-48ed-bc9d-0c583ec2af49', '3104bc34-d83b-4139-9008-87f180ac6259', 'FACILITIES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('9fc1e5b6-1488-4bdd-8ba5-5f248c359e10', '3104bc34-d83b-4139-9008-87f180ac6259', 'GEOGRAPHIC_ZONES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('9716cc55-f621-49de-99ce-c98b38e0556b', '3104bc34-d83b-4139-9008-87f180ac6259', 'SUPERVISORY_NODES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('be885305-1a8b-4714-8cfd-414199be3956', '3104bc34-d83b-4139-9008-87f180ac6259', 'PRODUCTS_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('f5394180-137c-4372-a40c-46216018ccac', '3104bc34-d83b-4139-9008-87f180ac6259', 'REQUISITION_TEMPLATES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('ffc306f0-bad2-43cb-9a19-05653ad2c245', '3104bc34-d83b-4139-9008-87f180ac6259', 'STOCK_CARD_TEMPLATES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('d1b1a45c-dc4b-4a3c-8cf1-4c01ad123960', '3104bc34-d83b-4139-9008-87f180ac6259', 'STOCK_SOURCES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('8f1ce563-4521-48c4-ae09-83203ef5f47b', '3104bc34-d83b-4139-9008-87f180ac6259', 'STOCK_DESTINATIONS_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('9467b3d2-dda0-402b-8385-8092a5000445', '3104bc34-d83b-4139-9008-87f180ac6259', 'STOCK_CARD_LINE_ITEM_REASONS_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('1fce40aa-76b5-4d58-a393-79d4a781d49e', '3104bc34-d83b-4139-9008-87f180ac6259', 'STOCK_ORGANIZATIONS_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('b2f7e41f-2a2c-4cca-bb22-b51368e3b0fe', '3104bc34-d83b-4139-9008-87f180ac6259', 'USER_ROLES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('c3c6367c-9168-4e63-bddf-bc70fd1fd5b7', '3104bc34-d83b-4139-9008-87f180ac6259', 'PROCESSING_SCHEDULES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('137017f1-68a8-42c9-9fe8-afff4e8ae01e', '3104bc34-d83b-4139-9008-87f180ac6259', 'USERS_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('b0d1c8cd-8bb8-4cd8-8440-336f52aa1fd1', '3104bc34-d83b-4139-9008-87f180ac6259', 'REQUISITION_GROUPS_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('773b5ef6-071a-440d-8f09-3e555e3afd0e', '3104bc34-d83b-4139-9008-87f180ac6259', 'ORDERABLES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('442e7603-1875-4db4-944d-6114f699dcdf', '3104bc34-d83b-4139-9008-87f180ac6259', 'STOCK_ADJUSTMENT_REASONS_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('facde1cb-2d8a-4e5f-a8b6-1ae48e9202e0', '3104bc34-d83b-4139-9008-87f180ac6259', 'FACILITY_APPROVED_ORDERABLES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('e464196c-f1fe-487f-81c8-10de2724c4e0', '3104bc34-d83b-4139-9008-87f180ac6259', 'SYSTEM_SETTINGS_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('9b7d7ae9-0d64-46a8-bb51-f23abbf7b7aa', '3104bc34-d83b-4139-9008-87f180ac6259', 'SUPPLY_LINES_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('75e109ce-875a-4bac-81a5-5e338e8b53a3', '3104bc34-d83b-4139-9008-87f180ac6259', 'CCE_MANAGE');
INSERT INTO referencedata.permission_strings (id, roleassignmentid, value) VALUES ('aa555fa6-2943-4ed2-898e-8e3189804f23', '3104bc34-d83b-4139-9008-87f180ac6259', 'RIGHTS_VIEW');
