-- new tables
CREATE TABLE supply_partners (
    id uuid NOT NULL,
    name text NOT NULL,
    code text NOT NULL
);

CREATE TABLE supply_partner_associations (
    id uuid NOT NULL,
    programId uuid NOT NULL,
    supervisoryNodeId uuid NOT NULL,
    supplyPartnerId uuid NOT NULL
);

CREATE TABLE supply_partner_association_facilities (
    supplyPartnerAssociationId uuid NOT NULL,
    facilityId uuid NOT NULL
);

CREATE TABLE supply_partner_association_orderables (
    supplyPartnerAssociationId uuid NOT NULL,
    orderableId uuid NOT NULL,
    orderableVersionId bigint NOT NULL
);

-- primary keys
ALTER TABLE ONLY supply_partners
  ADD CONSTRAINT supply_partners_pkey PRIMARY KEY (id);

ALTER TABLE ONLY supply_partner_associations
  ADD CONSTRAINT supply_partner_associations_pkey PRIMARY KEY (id);

ALTER TABLE ONLY supply_partner_association_facilities
  ADD CONSTRAINT supply_partner_association_facilities_pkey
  PRIMARY KEY (supplyPartnerAssociationId, facilityId);

ALTER TABLE ONLY supply_partner_association_orderables
  ADD CONSTRAINT supply_partner_association_orderables_pkey
  PRIMARY KEY (supplyPartnerAssociationId, orderableId, orderableVersionId);

-- foreign keys
ALTER TABLE ONLY supply_partner_associations
    ADD CONSTRAINT supply_partner_associations_supplypartnerid_fkey
    FOREIGN KEY (supplyPartnerId)
    REFERENCES supply_partners(id);

ALTER TABLE ONLY supply_partner_association_facilities
    ADD CONSTRAINT supply_partner_association_facilities_associationid_fkey
    FOREIGN KEY (supplyPartnerAssociationId)
    REFERENCES supply_partner_associations(id);
ALTER TABLE ONLY supply_partner_association_facilities
    ADD CONSTRAINT supply_partner_association_facilitiesfacilityId_fkey
    FOREIGN KEY (facilityId)
    REFERENCES facilities(id);

ALTER TABLE ONLY supply_partner_association_orderables
    ADD CONSTRAINT supply_partner_association_orderables_associationid_fkey
    FOREIGN KEY (supplyPartnerAssociationId)
    REFERENCES supply_partner_associations(id);
ALTER TABLE ONLY supply_partner_association_orderables
    ADD CONSTRAINT supply_partner_association_orderables
    FOREIGN KEY (orderableId, orderableVersionId)
    REFERENCES orderables(id, versionid);

-- index
CREATE UNIQUE INDEX unq_supply_partner_code
ON supply_partners (lower(code));
