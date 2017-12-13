CREATE TABLE service_accounts (
    id uuid NOT NULL,
    login character varying(255) NOT NULL,
    createdBy uuid NOT NULL,
    createdDate timestamp with time zone NOT NULL
);

ALTER TABLE ONLY service_accounts
    ADD CONSTRAINT service_accounts_pkey PRIMARY KEY (id);