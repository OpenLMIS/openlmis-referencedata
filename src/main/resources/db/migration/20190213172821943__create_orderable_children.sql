-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE TABLE orderable_children
(
  id                 uuid   NOT NULL PRIMARY KEY,
  parentId           uuid   not null,
  parentVersionId    BIGINT not null,
  orderableId        uuid   not null,
  orderableVersionId BIGINT not null,
  quantity           BIGINT not null
);

ALTER TABLE orderable_children
  ADD CONSTRAINT unq_orderable_parent_id
    UNIQUE (orderableid, orderableVersionId, parentid, parentVersionId);

ALTER TABLE orderable_children
  ADD FOREIGN KEY (parentId, parentVersionId)
    REFERENCES orderables (id, versionId);

ALTER TABLE orderable_children
  ADD FOREIGN KEY (orderableId, orderableVersionId)
    REFERENCES orderables (id, versionId);