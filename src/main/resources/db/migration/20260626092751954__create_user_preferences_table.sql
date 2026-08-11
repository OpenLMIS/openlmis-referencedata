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
