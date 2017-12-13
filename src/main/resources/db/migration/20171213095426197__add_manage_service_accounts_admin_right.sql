--
-- Data for Name: rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO rights (id, description, name, type) VALUES ('5dcca42a-dfe3-11e7-80c1-9a214cf093ae', NULL, 'SERVICE_ACCOUNTS_MANAGE', 'GENERAL_ADMIN');

--
-- Data for Name: role_rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '5dcca42a-dfe3-11e7-80c1-9a214cf093ae');
