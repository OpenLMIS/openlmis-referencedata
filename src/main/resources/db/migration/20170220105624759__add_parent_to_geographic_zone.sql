ALTER TABLE referencedata.geographic_zones ADD COLUMN parentid uuid;
ALTER TABLE referencedata.geographic_zones ADD CONSTRAINT geographic_zones_parentid_fkey FOREIGN KEY (parentid) REFERENCES referencedata.geographic_zones(id);
