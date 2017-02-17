--
-- Data for Name: rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO rights (id, description, name, type) VALUES ('f55c5bda-eeb1-4c34-b80a-761d9a920f81', NULL, 'STOCK_ADJUSTMENT_REASONS_MANAGE', 'GENERAL_ADMIN');

--
-- Data for Name: role_rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO referencedata.role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'f55c5bda-eeb1-4c34-b80a-761d9a920f81');
