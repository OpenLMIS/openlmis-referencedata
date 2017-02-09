--
-- Data for Name: rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO rights (id, description, name, type) VALUES ('68bba98b-7d7c-41b0-bca5-7d30e87c5cff', NULL, 'SYSTEM_SETTINGS_MANAGE', 'GENERAL_ADMIN');

--
-- Data for Name: role_rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '68bba98b-7d7c-41b0-bca5-7d30e87c5cff');

