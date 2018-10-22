--
-- Data for Name: rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO rights (id, description, name, type) VALUES ('c5129de2-aa9d-40c2-948d-5f0d881b0279', NULL, 'SUPPLY_PARTNER_MANAGE', 'GENERAL_ADMIN');

--
-- Data for Name: role_rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'c5129de2-aa9d-40c2-948d-5f0d881b0279');
