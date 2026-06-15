-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE TABLE user_preferences (
    id uuid NOT NULL,
    userid uuid NOT NULL,
    preferencekey text NOT NULL,
    preferencevalue text NOT NULL
);

ALTER TABLE ONLY user_preferences
  ADD CONSTRAINT user_preferences_pkey PRIMARY KEY (id);

ALTER TABLE user_preferences
  ADD CONSTRAINT user_preferences_userid_fkey FOREIGN KEY (userid)
  REFERENCES referencedata.users(id) ON DELETE CASCADE;

ALTER TABLE user_preferences
  ADD CONSTRAINT user_preferences_userid_key_unique UNIQUE (userid, preferencekey);
