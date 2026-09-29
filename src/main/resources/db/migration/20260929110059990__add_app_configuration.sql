-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE TABLE app_configuration (
    id uuid PRIMARY KEY CHECK (id = '5c3d6b1e-0000-4000-8000-000000000001'),
    appname varchar(64),
    themepreset varchar(32),
    defaultappearance varchar(16) CHECK (defaultappearance IN ('light', 'dark', 'system')),
    featureflags jsonb,
    logosha256 varchar(64),
    logocontenttype varchar(32),
    logosize integer,
    version bigint NOT NULL DEFAULT 0,
    modifieddate timestamp with time zone NOT NULL DEFAULT now()
);

CREATE TABLE app_configuration_logos (
    sha256 varchar(64) PRIMARY KEY,
    contenttype varchar(32) NOT NULL,
    data bytea NOT NULL
);

INSERT INTO app_configuration (id) VALUES ('5c3d6b1e-0000-4000-8000-000000000001');
