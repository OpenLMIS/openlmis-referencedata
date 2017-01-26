--
-- Name: facilities; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE facilities (
    id uuid NOT NULL,
    active boolean NOT NULL,
    code text NOT NULL,
    comment text,
    description text,
    enabled boolean NOT NULL,
    godowndate date,
    golivedate date,
    name text,
    openlmisaccessible boolean,
    geographiczoneid uuid NOT NULL,
    operatedbyid uuid,
    typeid uuid NOT NULL
);


ALTER TABLE facilities OWNER TO postgres;

--
-- Name: facility_operators; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE facility_operators (
    id uuid NOT NULL,
    code text NOT NULL,
    description text,
    displayorder integer,
    name text
);


ALTER TABLE facility_operators OWNER TO postgres;

--
-- Name: facility_type_approved_products; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE facility_type_approved_products (
    id uuid NOT NULL,
    emergencyorderpoint double precision,
    maxmonthsofstock double precision NOT NULL,
    minmonthsofstock double precision,
    facilitytypeid uuid NOT NULL,
    programproductid uuid NOT NULL
);


ALTER TABLE facility_type_approved_products OWNER TO postgres;

--
-- Name: facility_types; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE facility_types (
    id uuid NOT NULL,
    active boolean,
    code text NOT NULL,
    description text,
    displayorder integer,
    name text
);


ALTER TABLE facility_types OWNER TO postgres;

--
-- Name: geographic_levels; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE geographic_levels (
    id uuid NOT NULL,
    code text NOT NULL,
    levelnumber integer NOT NULL,
    name text
);


ALTER TABLE geographic_levels OWNER TO postgres;

--
-- Name: geographic_zones; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE geographic_zones (
    id uuid NOT NULL,
    catchmentpopulation integer,
    code text NOT NULL,
    latitude numeric(8,5),
    longitude numeric(8,5),
    name text,
    levelid uuid NOT NULL
);


ALTER TABLE geographic_zones OWNER TO postgres;

--
-- Name: orderable_products; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE orderable_products (
    type character varying(31) NOT NULL,
    id uuid NOT NULL,
    dispensingunit character varying(255),
    name character varying(255),
    packroundingthreshold bigint NOT NULL,
    packsize bigint NOT NULL,
    code character varying(255),
    roundtozero boolean NOT NULL,
    description character varying(255),
    manufacturer character varying(255),
    globalproductid uuid
);


ALTER TABLE orderable_products OWNER TO postgres;

--
-- Name: processing_periods; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE processing_periods (
    id uuid NOT NULL,
    description text,
    enddate date NOT NULL,
    name text NOT NULL,
    startdate date NOT NULL,
    processingscheduleid uuid NOT NULL
);


ALTER TABLE processing_periods OWNER TO postgres;

--
-- Name: processing_schedules; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE processing_schedules (
    id uuid NOT NULL,
    code text NOT NULL,
    description text,
    modifieddate timestamp with time zone,
    name text NOT NULL
);


ALTER TABLE processing_schedules OWNER TO postgres;

--
-- Name: product_categories; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE product_categories (
    id uuid NOT NULL,
    code character varying(255),
    displayname character varying(255),
    displayorder integer NOT NULL
);


ALTER TABLE product_categories OWNER TO postgres;

--
-- Name: program_products; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE program_products (
    id uuid NOT NULL,
    active boolean NOT NULL,
    displayorder integer NOT NULL,
    dosespermonth integer,
    fullsupply boolean NOT NULL,
    maxmonthsstock integer NOT NULL,
    priceperpack numeric(19,2),
    productid uuid NOT NULL,
    productcategoryid uuid NOT NULL,
    programid uuid NOT NULL
);


ALTER TABLE program_products OWNER TO postgres;

--
-- Name: programs; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE programs (
    id uuid NOT NULL,
    active boolean,
    code character varying(255),
    description text,
    name text,
    periodsskippable boolean NOT NULL,
    shownonfullsupplytab boolean
);


ALTER TABLE programs OWNER TO postgres;

--
-- Name: requisition_group_members; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE requisition_group_members (
    requisitiongroupid uuid NOT NULL,
    facilityid uuid NOT NULL
);


ALTER TABLE requisition_group_members OWNER TO postgres;

--
-- Name: requisition_group_program_schedules; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE requisition_group_program_schedules (
    id uuid NOT NULL,
    directdelivery boolean NOT NULL,
    dropofffacilityid uuid,
    processingscheduleid uuid NOT NULL,
    programid uuid NOT NULL,
    requisitiongroupid uuid NOT NULL
);


ALTER TABLE requisition_group_program_schedules OWNER TO postgres;

--
-- Name: requisition_groups; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE requisition_groups (
    id uuid NOT NULL,
    code text NOT NULL,
    description text,
    name text NOT NULL,
    supervisorynodeid uuid NOT NULL
);


ALTER TABLE requisition_groups OWNER TO postgres;

--
-- Name: right_attachments; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE right_attachments (
    rightid uuid NOT NULL,
    attachmentid uuid NOT NULL
);


ALTER TABLE right_attachments OWNER TO postgres;

--
-- Name: rights; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE rights (
    id uuid NOT NULL,
    description text,
    name text NOT NULL,
    type text NOT NULL
);


ALTER TABLE rights OWNER TO postgres;

--
-- Name: role_assignments; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE role_assignments (
    type character varying(31) NOT NULL,
    id uuid NOT NULL,
    roleid uuid,
    userid uuid,
    warehouseid uuid,
    programid uuid,
    supervisorynodeid uuid
);


ALTER TABLE role_assignments OWNER TO postgres;

--
-- Name: role_rights; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE role_rights (
    roleid uuid NOT NULL,
    rightid uuid NOT NULL
);


ALTER TABLE role_rights OWNER TO postgres;

--
-- Name: roles; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE roles (
    id uuid NOT NULL,
    description text,
    name text NOT NULL
);


ALTER TABLE roles OWNER TO postgres;

--
-- Name: stock_adjustment_reasons; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE stock_adjustment_reasons (
    id uuid NOT NULL,
    additive boolean,
    description text,
    displayorder integer,
    name text NOT NULL,
    programid uuid NOT NULL
);


ALTER TABLE stock_adjustment_reasons OWNER TO postgres;

--
-- Name: supervisory_nodes; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE supervisory_nodes (
    id uuid NOT NULL,
    code text NOT NULL,
    description text,
    name text,
    facilityid uuid NOT NULL,
    parentid uuid
);


ALTER TABLE supervisory_nodes OWNER TO postgres;

--
-- Name: supply_lines; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE supply_lines (
    id uuid NOT NULL,
    description text,
    programid uuid NOT NULL,
    supervisorynodeid uuid NOT NULL,
    supplyingfacilityid uuid NOT NULL
);


ALTER TABLE supply_lines OWNER TO postgres;

--
-- Name: supported_programs; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE supported_programs (
    id uuid NOT NULL,
    active boolean NOT NULL,
    startdate date,
    facilityid uuid NOT NULL,
    programid uuid NOT NULL
);


ALTER TABLE supported_programs OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    id uuid NOT NULL,
    active boolean DEFAULT false NOT NULL,
    email character varying(255) NOT NULL,
    extradata jsonb,
    firstname text NOT NULL,
    lastname text NOT NULL,
    loginrestricted boolean DEFAULT false NOT NULL,
    timezone character varying(255),
    username text NOT NULL,
    verified boolean DEFAULT false NOT NULL,
    homefacilityid uuid
);


ALTER TABLE users OWNER TO postgres;

--
-- Data for Name: facilities; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: facility_operators; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: facility_type_approved_products; Type: TABLE DATA; Schema: referencedata; Owner: postgres
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
-- Data for Name: orderable_products; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: processing_periods; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: processing_schedules; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: product_categories; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: program_products; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: programs; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: requisition_group_members; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: requisition_group_program_schedules; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: requisition_groups; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: right_attachments; Type: TABLE DATA; Schema: referencedata; Owner: postgres
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
INSERT INTO rights (id, description, name, type) VALUES ('ebad51c3-f7c3-4fab-97e1-839973b045d4', NULL, 'USER_ROLES_MANAGE', 'GENERAL_ADMIN');
INSERT INTO rights (id, description, name, type) VALUES ('9ade922b-3523-4582-bef4-a47701f7df14', NULL, 'REQUISITION_CREATE', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('bffa2de2-dc2a-47dd-b126-6501748ac3fc', NULL, 'REQUISITION_APPROVE', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('feb4c0b8-f6d2-4289-b29d-811c1d0b2863', NULL, 'REQUISITION_AUTHORIZE', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('c3eb5df0-c3ac-4e70-a978-02827462f60e', NULL, 'REQUISITION_DELETE', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('e101d2b8-6a0f-4af6-a5de-a9576b4ebc50', NULL, 'REQUISITION_VIEW', 'SUPERVISION');
INSERT INTO rights (id, description, name, type) VALUES ('7958129d-c4c0-4294-a40c-c2b07cb8e515', NULL, 'REQUISITION_CONVERT_TO_ORDER', 'ORDER_FULFILLMENT');
INSERT INTO rights (id, description, name, type) VALUES ('65626c3d-513f-4255-93fd-808709860594', NULL, 'FULFILLMENT_TRANSFER_ORDER', 'ORDER_FULFILLMENT');


--
-- Data for Name: role_assignments; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO role_assignments (type, id, roleid, userid, warehouseid, programid, supervisorynodeid) VALUES ('direct', '3104bc34-d83b-4139-9008-87f180ac6259', 'a439c5de-b8aa-11e6-80f5-76304dec7eb7', '35316636-6264-6331-2d34-3933322d3462', NULL, NULL, NULL);


--
-- Data for Name: role_rights; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'e96017ff-af8c-4313-a070-caa70465c949');
INSERT INTO role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '8816edba-b8a9-11e6-80f5-76304dec7eb7');
INSERT INTO role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '4bed4f40-36b5-42a7-94c9-0fd3d4252374');
INSERT INTO role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '4e731cf7-854f-4af7-9ea4-bd5d8ed7bb22');
INSERT INTO role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'ebad51c3-f7c3-4fab-97e1-839973b045d4');
INSERT INTO role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', '5c4b3b9b-713e-4b9a-8c58-7efcd2954512');
INSERT INTO role_rights (roleid, rightid) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', 'fb6a0053-6254-4b41-8028-bf91421f90dd');


--
-- Data for Name: roles; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO roles (id, description, name) VALUES ('a439c5de-b8aa-11e6-80f5-76304dec7eb7', NULL, 'System Administrator');


--
-- Data for Name: stock_adjustment_reasons; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: supervisory_nodes; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: supply_lines; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: supported_programs; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--



--
-- Data for Name: users; Type: TABLE DATA; Schema: referencedata; Owner: postgres
--

INSERT INTO users (id, active, email, extradata, firstname, lastname, loginrestricted, timezone, username, verified, homefacilityid) VALUES ('35316636-6264-6331-2d34-3933322d3462', false, 'example@mail.com', NULL, 'Admin', 'User', false, 'UTC', 'admin', false, NULL);


--
-- Name: facilities_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY facilities
    ADD CONSTRAINT facilities_pkey PRIMARY KEY (id);


--
-- Name: facility_operators_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY facility_operators
    ADD CONSTRAINT facility_operators_pkey PRIMARY KEY (id);


--
-- Name: facility_type_approved_products_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY facility_type_approved_products
    ADD CONSTRAINT facility_type_approved_products_pkey PRIMARY KEY (id);


--
-- Name: facility_types_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY facility_types
    ADD CONSTRAINT facility_types_pkey PRIMARY KEY (id);


--
-- Name: geographic_levels_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY geographic_levels
    ADD CONSTRAINT geographic_levels_pkey PRIMARY KEY (id);


--
-- Name: geographic_zones_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY geographic_zones
    ADD CONSTRAINT geographic_zones_pkey PRIMARY KEY (id);


--
-- Name: orderable_products_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY orderable_products
    ADD CONSTRAINT orderable_products_pkey PRIMARY KEY (id);


--
-- Name: processing_periods_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY processing_periods
    ADD CONSTRAINT processing_periods_pkey PRIMARY KEY (id);


--
-- Name: processing_schedules_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY processing_schedules
    ADD CONSTRAINT processing_schedules_pkey PRIMARY KEY (id);


--
-- Name: product_categories_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY product_categories
    ADD CONSTRAINT product_categories_pkey PRIMARY KEY (id);


--
-- Name: program_products_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY program_products
    ADD CONSTRAINT program_products_pkey PRIMARY KEY (id);


--
-- Name: programs_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY programs
    ADD CONSTRAINT programs_pkey PRIMARY KEY (id);


--
-- Name: requisition_group_members_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY requisition_group_members
    ADD CONSTRAINT requisition_group_members_pkey PRIMARY KEY (requisitiongroupid, facilityid);


--
-- Name: requisition_group_program_schedule_unique_program_requisitiongr; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY requisition_group_program_schedules
    ADD CONSTRAINT requisition_group_program_schedule_unique_program_requisitiongr UNIQUE (requisitiongroupid, programid);


--
-- Name: requisition_group_program_schedules_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY requisition_group_program_schedules
    ADD CONSTRAINT requisition_group_program_schedules_pkey PRIMARY KEY (id);


--
-- Name: requisition_groups_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY requisition_groups
    ADD CONSTRAINT requisition_groups_pkey PRIMARY KEY (id);


--
-- Name: right_attachments_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY right_attachments
    ADD CONSTRAINT right_attachments_pkey PRIMARY KEY (rightid, attachmentid);


--
-- Name: rights_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY rights
    ADD CONSTRAINT rights_pkey PRIMARY KEY (id);


--
-- Name: role_assignments_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT role_assignments_pkey PRIMARY KEY (id);


--
-- Name: role_rights_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY role_rights
    ADD CONSTRAINT role_rights_pkey PRIMARY KEY (roleid, rightid);


--
-- Name: roles_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: stock_adjustment_reasons_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY stock_adjustment_reasons
    ADD CONSTRAINT stock_adjustment_reasons_pkey PRIMARY KEY (id);


--
-- Name: supervisory_nodes_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY supervisory_nodes
    ADD CONSTRAINT supervisory_nodes_pkey PRIMARY KEY (id);


--
-- Name: supply_lines_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY supply_lines
    ADD CONSTRAINT supply_lines_pkey PRIMARY KEY (id);


--
-- Name: supported_programs_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY supported_programs
    ADD CONSTRAINT supported_programs_pkey PRIMARY KEY (id);


--
-- Name: uk_4f64k9vkx833wfpw8n25x2602; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY rights
    ADD CONSTRAINT uk_4f64k9vkx833wfpw8n25x2602 UNIQUE (name);


--
-- Name: uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: uk_8hnxrslwj69io4240g3gaogw3; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY processing_schedules
    ADD CONSTRAINT uk_8hnxrslwj69io4240g3gaogw3 UNIQUE (name);


--
-- Name: uk_9vforn7hxhuinr8bmu0vkad3v; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY supervisory_nodes
    ADD CONSTRAINT uk_9vforn7hxhuinr8bmu0vkad3v UNIQUE (code);


--
-- Name: uk_by9o3bl6rafeuane589514s2v; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY geographic_levels
    ADD CONSTRAINT uk_by9o3bl6rafeuane589514s2v UNIQUE (code);


--
-- Name: uk_dkdb51mfvy91h64s1x6wj3spw; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY processing_schedules
    ADD CONSTRAINT uk_dkdb51mfvy91h64s1x6wj3spw UNIQUE (code);


--
-- Name: uk_g7ooo22v3vokh2qrqbxw7uaps; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY facility_operators
    ADD CONSTRAINT uk_g7ooo22v3vokh2qrqbxw7uaps UNIQUE (code);


--
-- Name: uk_jpns3ahywgm4k52rdfm08m9k0; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY geographic_zones
    ADD CONSTRAINT uk_jpns3ahywgm4k52rdfm08m9k0 UNIQUE (code);


--
-- Name: uk_mnsci7u7h2r2b3tohhn0b819; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY facilities
    ADD CONSTRAINT uk_mnsci7u7h2r2b3tohhn0b819 UNIQUE (code);


--
-- Name: uk_nfppl8ui0vgjoxenm5v2727wo; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY facility_types
    ADD CONSTRAINT uk_nfppl8ui0vgjoxenm5v2727wo UNIQUE (code);


--
-- Name: uk_nrqjt84p9wmrm1qmr7nokj8sg; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY requisition_groups
    ADD CONSTRAINT uk_nrqjt84p9wmrm1qmr7nokj8sg UNIQUE (code);


--
-- Name: uk_ofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT uk_ofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- Name: uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: fk2tcq3p7atk25pe8xmdbuwy2wo; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY supply_lines
    ADD CONSTRAINT fk2tcq3p7atk25pe8xmdbuwy2wo FOREIGN KEY (supplyingfacilityid) REFERENCES facilities(id);


--
-- Name: fk2u2ivdivj2nkctl3e1btgelvg; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY requisition_groups
    ADD CONSTRAINT fk2u2ivdivj2nkctl3e1btgelvg FOREIGN KEY (supervisorynodeid) REFERENCES supervisory_nodes(id);


--
-- Name: fk2uuqsp7ofjrvkwd7gpgvn9916; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT fk2uuqsp7ofjrvkwd7gpgvn9916 FOREIGN KEY (warehouseid) REFERENCES facilities(id);


--
-- Name: fk2vn7d69cbm7cl3m4rte8cy6ja; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY facilities
    ADD CONSTRAINT fk2vn7d69cbm7cl3m4rte8cy6ja FOREIGN KEY (geographiczoneid) REFERENCES geographic_zones(id);


--
-- Name: fk3lhot2j862re7kndwhthe7dnt; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY requisition_group_program_schedules
    ADD CONSTRAINT fk3lhot2j862re7kndwhthe7dnt FOREIGN KEY (dropofffacilityid) REFERENCES facilities(id);


--
-- Name: fk3wu6tbyjf7re179s3v57d0hqw; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY geographic_zones
    ADD CONSTRAINT fk3wu6tbyjf7re179s3v57d0hqw FOREIGN KEY (levelid) REFERENCES geographic_levels(id);


--
-- Name: fk40dovhydsykbqbs0yg2qkg7n8; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY requisition_group_program_schedules
    ADD CONSTRAINT fk40dovhydsykbqbs0yg2qkg7n8 FOREIGN KEY (requisitiongroupid) REFERENCES requisition_groups(id);


--
-- Name: fk5dmm5ettanr8rw408jyv4mux0; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY requisition_group_members
    ADD CONSTRAINT fk5dmm5ettanr8rw408jyv4mux0 FOREIGN KEY (requisitiongroupid) REFERENCES requisition_groups(id);


--
-- Name: fk5oymdkhd5bylwwqavf2q6o8ny; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY facility_type_approved_products
    ADD CONSTRAINT fk5oymdkhd5bylwwqavf2q6o8ny FOREIGN KEY (programproductid) REFERENCES program_products(id);


--
-- Name: fk5yvqwsfj7a21e34vp9rb13ibj; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY requisition_group_program_schedules
    ADD CONSTRAINT fk5yvqwsfj7a21e34vp9rb13ibj FOREIGN KEY (processingscheduleid) REFERENCES processing_schedules(id);


--
-- Name: fk7bq08uarqixbaytuyjtsg45mv; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT fk7bq08uarqixbaytuyjtsg45mv FOREIGN KEY (userid) REFERENCES users(id);


--
-- Name: fk7tf8gu3jng8p1xgyt8obnavks; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY right_attachments
    ADD CONSTRAINT fk7tf8gu3jng8p1xgyt8obnavks FOREIGN KEY (rightid) REFERENCES rights(id);


--
-- Name: fk81f9bol3noopwf1em9t21e6c6; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY requisition_group_program_schedules
    ADD CONSTRAINT fk81f9bol3noopwf1em9t21e6c6 FOREIGN KEY (programid) REFERENCES programs(id);


--
-- Name: fk8clgdfo4j2nsb6ssxs3u0cp8n; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY facility_type_approved_products
    ADD CONSTRAINT fk8clgdfo4j2nsb6ssxs3u0cp8n FOREIGN KEY (facilitytypeid) REFERENCES facility_types(id);


--
-- Name: fk90lvhcdt9hg4ru1ao38e45x0s; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY requisition_group_members
    ADD CONSTRAINT fk90lvhcdt9hg4ru1ao38e45x0s FOREIGN KEY (facilityid) REFERENCES facilities(id);


--
-- Name: fk986t770e98w262b6ram6m2oi3; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY supported_programs
    ADD CONSTRAINT fk986t770e98w262b6ram6m2oi3 FOREIGN KEY (facilityid) REFERENCES facilities(id);


--
-- Name: fk9esnupki24cc8i9hwjryvuym3; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY role_rights
    ADD CONSTRAINT fk9esnupki24cc8i9hwjryvuym3 FOREIGN KEY (roleid) REFERENCES roles(id);


--
-- Name: fk9qt21xn4mnehgra2y1ajelx51; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY supported_programs
    ADD CONSTRAINT fk9qt21xn4mnehgra2y1ajelx51 FOREIGN KEY (programid) REFERENCES programs(id);


--
-- Name: fkago6b65vqo0xapciruk8jqur1; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY right_attachments
    ADD CONSTRAINT fkago6b65vqo0xapciruk8jqur1 FOREIGN KEY (attachmentid) REFERENCES rights(id);


--
-- Name: fkbcgrqydf79ingrbb1b8qttp9t; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY supply_lines
    ADD CONSTRAINT fkbcgrqydf79ingrbb1b8qttp9t FOREIGN KEY (supervisorynodeid) REFERENCES supervisory_nodes(id);


--
-- Name: fkg49ujisk0joauiklj0vlldm35; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fkg49ujisk0joauiklj0vlldm35 FOREIGN KEY (homefacilityid) REFERENCES facilities(id);


--
-- Name: fkh9gh9d7vlgco9n7h3daallwd2; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY stock_adjustment_reasons
    ADD CONSTRAINT fkh9gh9d7vlgco9n7h3daallwd2 FOREIGN KEY (programid) REFERENCES programs(id);


--
-- Name: fkhawcmr9fx4f9032iygb1xndti; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY role_rights
    ADD CONSTRAINT fkhawcmr9fx4f9032iygb1xndti FOREIGN KEY (rightid) REFERENCES rights(id);


--
-- Name: fkhib37s2ph4w22wqdu1iyrf439; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT fkhib37s2ph4w22wqdu1iyrf439 FOREIGN KEY (programid) REFERENCES programs(id);


--
-- Name: fkivotepe50ydd4ixp6u4wev8ks; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY program_products
    ADD CONSTRAINT fkivotepe50ydd4ixp6u4wev8ks FOREIGN KEY (productcategoryid) REFERENCES product_categories(id);


--
-- Name: fkjbgjp8u70m6wtmg45c02f8y1y; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY orderable_products
    ADD CONSTRAINT fkjbgjp8u70m6wtmg45c02f8y1y FOREIGN KEY (globalproductid) REFERENCES orderable_products(id);


--
-- Name: fkkcrn1ejnd2cmaf3bmfbwlnbv8; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY program_products
    ADD CONSTRAINT fkkcrn1ejnd2cmaf3bmfbwlnbv8 FOREIGN KEY (productid) REFERENCES orderable_products(id);


--
-- Name: fkkg17afgncqqlfht3u37cfl7d6; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY supply_lines
    ADD CONSTRAINT fkkg17afgncqqlfht3u37cfl7d6 FOREIGN KEY (programid) REFERENCES programs(id);


--
-- Name: fkm12nqtk6paxb7b20m5rklm12w; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY facilities
    ADD CONSTRAINT fkm12nqtk6paxb7b20m5rklm12w FOREIGN KEY (operatedbyid) REFERENCES facility_operators(id);


--
-- Name: fkmhstjposjfpa0m77v89gy0q2b; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT fkmhstjposjfpa0m77v89gy0q2b FOREIGN KEY (supervisorynodeid) REFERENCES supervisory_nodes(id);


--
-- Name: fkmuld4symcrudni1xc70pngxdm; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY program_products
    ADD CONSTRAINT fkmuld4symcrudni1xc70pngxdm FOREIGN KEY (programid) REFERENCES programs(id);


--
-- Name: fkn4fxuvpiasbtskg8avi5pnff0; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY processing_periods
    ADD CONSTRAINT fkn4fxuvpiasbtskg8avi5pnff0 FOREIGN KEY (processingscheduleid) REFERENCES processing_schedules(id);


--
-- Name: fknk908araoyndgd7uwsedn0ddh; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY role_assignments
    ADD CONSTRAINT fknk908araoyndgd7uwsedn0ddh FOREIGN KEY (roleid) REFERENCES roles(id);


--
-- Name: fkpc0soanvqabccyg5br9aexoc1; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY facilities
    ADD CONSTRAINT fkpc0soanvqabccyg5br9aexoc1 FOREIGN KEY (typeid) REFERENCES facility_types(id);


--
-- Name: fkpjjkmuhcksc8mhfsnfxf8d9fh; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY supervisory_nodes
    ADD CONSTRAINT fkpjjkmuhcksc8mhfsnfxf8d9fh FOREIGN KEY (parentid) REFERENCES supervisory_nodes(id);


--
-- Name: fksaovlf1j7vrxvabbiyl1mt37t; Type: FK CONSTRAINT; Schema: referencedata; Owner: postgres
--

ALTER TABLE ONLY supervisory_nodes
    ADD CONSTRAINT fksaovlf1j7vrxvabbiyl1mt37t FOREIGN KEY (facilityid) REFERENCES facilities(id);
