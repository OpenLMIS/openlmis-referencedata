CREATE TABLE integration_emails (
    id uuid NOT NULL,
    email text NOT NULL,
    CONSTRAINT integration_emails_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX unq_integration_email
ON integration_emails (LOWER(email))
WHERE email IS NOT NULL;
