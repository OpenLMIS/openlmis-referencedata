ALTER TABLE supervisory_nodes ADD COLUMN partnerId uuid;
ALTER TABLE supervisory_nodes ADD CONSTRAINT supervisory_nodes_partnerId_fkey FOREIGN KEY (partnerId) REFERENCES supervisory_nodes(id);
