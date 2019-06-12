-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE TABLE system_notifications (
    id uuid NOT NULL,
    title character varying(255),
    message text NOT NULL,
    startdate timestamp with time zone,
    createddate timestamp with time zone NOT NULL,
    expirydate timestamp with time zone,
    active boolean NOT NULL DEFAULT true,
    authorid uuid NOT NULL
);

ALTER TABLE ONLY system_notifications
  ADD CONSTRAINT system_notifications_pkey PRIMARY KEY (id);

ALTER TABLE system_notifications
  ADD CONSTRAINT system_notifications_fkey FOREIGN KEY (authorid)
  REFERENCES referencedata.users(id);

CREATE INDEX ON system_notifications (active, authorid);