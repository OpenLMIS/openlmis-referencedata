CREATE TABLE integration_emails (
    id uuid NOT NULL,
    email text NOT NULL
);

CREATE UNIQUE INDEX unq_integration_email
ON integration_emails (LOWER(email))
WHERE email IS NOT NULL;