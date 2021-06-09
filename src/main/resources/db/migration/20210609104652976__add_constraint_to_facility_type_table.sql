--
-- Name: facility_types_uk_name; Type: CONSTRAINT; Schema: referencedata; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY facility_types
    ADD CONSTRAINT facility_types_uk_name UNIQUE (name);