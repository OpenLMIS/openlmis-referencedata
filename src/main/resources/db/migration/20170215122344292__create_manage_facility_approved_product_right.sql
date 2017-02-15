--
-- Data for Name: rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO rights (id, description, name, type) VALUES ('e21ea917-19d2-404d-85cc-9344b9232b86', NULL, 'FACILITY_APPROVED_PRODUCT_MANAGE', 'GENERAL_ADMIN');

--
-- Data for Name: role_rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'e21ea917-19d2-404d-85cc-9344b9232b86');

