--
-- Data for Name: facility_operators; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: facility_types; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: geographic_levels; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: geographic_zones; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: facilities; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: orderable_display_categories; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: orderables; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: programs; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: program_orderables; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: facility_type_approved_products; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: processing_schedules; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: processing_periods; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: supervisory_nodes; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: requisition_groups; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: requisition_group_members; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: requisition_group_program_schedules; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO rights (id, description, name, type) VALUES ('e96017ff-af8c-4313-a070-caa70465c949', NULL, 'FACILITIES_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('4e731cf7-854f-4af7-9ea4-bd5d8ed7bb22', NULL, 'GEOGRAPHIC_ZONES_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('5c4b3b9b-713e-4b9a-8c58-7efcd2954512', NULL, 'SUPERVISORY_NODES_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('fb6a0053-6254-4b41-8028-bf91421f90dd', NULL, 'PRODUCTS_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('8816edba-b8a9-11e6-80f5-76304dec7eb7', NULL, 'REQUISITION_TEMPLATES_MANAGE', 'GENERAL_ADMIN');

INSERT INTO rights (id, description, name, type) VALUES ('4bed4f40-36b5-42a7-94c9-0fd3d4252374', NULL, 'STOCK_CARD_TEMPLATES_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('1f917f8c-ab2d-4b5f-b8ca-efb0c13cf6b0', NULL, 'STOCK_SOURCES_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('985a8039-a6a6-4a7b-844a-6d4591e77e1d', NULL, 'STOCK_DESTINATIONS_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('8973115f-81f7-4020-931c-a62209c2a963', NULL, 'STOCK_CARD_LINE_ITEM_REASONS_VIEW', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('17651f6d-39f3-41b6-adc8-4e3d901ac42c', NULL, 'STOCK_CARD_LINE_ITEM_REASONS_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('f76125b6-87e3-491d-aa5c-6f61b3cd88b7', NULL, 'ORGANIZATIONS_MANAGE', 'GENERAL_ADMIN');

INSERT INTO rights (id, description, name, type) VALUES ('ebad51c3-f7c3-4fab-97e1-839973b045d4', NULL, 'USER_ROLES_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('42791f7a-84a1-470b-bc3c-4160bc99f13c', NULL, 'PROCESSING_SCHEDULES_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('3687ea98-8a1e-4347-984c-3fd97d072066', NULL, 'USERS_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('c996d0c6-4f7d-46b5-a376-6f93a3071cb0', NULL, 'REQUISITION_GROUPS_MANAGE', 'GENERAL_ADMIN');

INSERT INTO rights (id, description, name, type) VALUES ('9ade922b-3523-4582-bef4-a47701f7df14', NULL, 'REQUISITION_CREATE', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('bffa2de2-dc2a-47dd-b126-6501748ac3fc', NULL, 'REQUISITION_APPROVE', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('feb4c0b8-f6d2-4289-b29d-811c1d0b2863', NULL, 'REQUISITION_AUTHORIZE', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('c3eb5df0-c3ac-4e70-a978-02827462f60e', NULL, 'REQUISITION_DELETE', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('e101d2b8-6a0f-4af6-a5de-a9576b4ebc50', NULL, 'REQUISITION_VIEW', 'SUPERVISION');

INSERT INTO rights (id, description, name, type) VALUES ('65626c3d-513f-4255-93fd-808709860594', NULL, 'ORDERS_TRANSFER', 'ORDER_FULFILLMENT');
INSERT INTO rights (id, description, name, type) VALUES ('24df2715-850c-4336-b650-90eb78c544bf', NULL, 'PODS_MANAGE', 'ORDER_FULFILLMENT');

INSERT INTO rights (id, description, name, type) VALUES ('35cf256f-1b28-41ad-84f5-99c158083dfc', NULL, 'REPORTS_VIEW', 'REPORTS');
INSERT INTO rights (id, description, name, type) VALUES ('60561639-4f94-4371-a5b5-ba95ecf7627f', NULL, 'REPORT_TEMPLATES_EDIT', 'REPORTS');

INSERT INTO rights (id, description, name, type) VALUES ('7b2da074-b754-4e49-bad3-2520651e5107', NULL, 'ORDERS_VIEW', 'ORDER_FULFILLMENT');
INSERT INTO rights (id, description, name, type) VALUES ('60580166-ab1c-464a-8401-08384efc57b7', NULL, 'ORDERS_EDIT', 'ORDER_FULFILLMENT');

INSERT INTO rights (id, description, name, type) VALUES ('6fb013fe-d878-43e9-bff0-fa5431e62c34', NULL, 'STOCK_INVENTORIES_EDIT', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('7b41c10e-5489-47a9-8a68-69ae74b8a4cf', NULL, 'STOCK_ADJUST', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('31cce55f-284b-4922-81bb-d8a9edc4c623', NULL, 'STOCK_CARDS_VIEW', 'SUPERVISION');

--
-- Data for Name: right_attachments; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: roles; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO referencedata.roles (id, description, name) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', NULL, 'System Administrator');


--
-- Data for Name: users; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO users (id, active, allownotify, email, extradata, firstname, lastname, loginrestricted, timezone, username, verified, homefacilityid) VALUES ('35316636-6264-6331-2d34-3933322d3462', false, true, 'example@mail.com', NULL, 'Admin', 'User', false, 'UTC', 'admin', false, NULL);


--
-- Data for Name: role_assignments; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO referencedata.role_assignments (type, id, roleid, userid, warehouseid, programid, supervisorynodeid) VALUES ('direct', '3104bc34-d83b-4139-9008-87f180ac6259', 'a439c5de-b8aa-11e6-80f5-76304dec7eb7', '35316636-6264-6331-2d34-3933322d3462', NULL, NULL, NULL);


--
-- Data for Name: role_rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'e96017ff-af8c-4313-a070-caa70465c949');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '8816edba-b8a9-11e6-80f5-76304dec7eb7');

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '4bed4f40-36b5-42a7-94c9-0fd3d4252374');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '1f917f8c-ab2d-4b5f-b8ca-efb0c13cf6b0');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '985a8039-a6a6-4a7b-844a-6d4591e77e1d');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '8973115f-81f7-4020-931c-a62209c2a963');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '17651f6d-39f3-41b6-adc8-4e3d901ac42c');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'f76125b6-87e3-491d-aa5c-6f61b3cd88b7');

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '4e731cf7-854f-4af7-9ea4-bd5d8ed7bb22');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'ebad51c3-f7c3-4fab-97e1-839973b045d4');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '5c4b3b9b-713e-4b9a-8c58-7efcd2954512');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'fb6a0053-6254-4b41-8028-bf91421f90dd');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '42791f7a-84a1-470b-bc3c-4160bc99f13c');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '3687ea98-8a1e-4347-984c-3fd97d072066');
INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'c996d0c6-4f7d-46b5-a376-6f93a3071cb0');


--
-- Data for Name: stock_adjustment_reasons; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: supply_lines; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: supported_programs; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--
