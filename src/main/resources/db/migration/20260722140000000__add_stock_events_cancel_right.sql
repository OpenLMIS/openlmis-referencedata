-- Right checked by the stockmanagement service when a user cancels an issue/receive movement.
-- SUPERVISION type so it can be granted for a user's home facility or the facilities they
-- supervise (per program). Inserted idempotently so re-running the migration set is safe.
INSERT INTO referencedata.rights (id, description, name, type)
SELECT CAST('bc0a9bc7-6875-4159-bdd6-7f4f6f43be9e' AS UUID), NULL, 'STOCK_EVENTS_CANCEL', 'SUPERVISION'
WHERE NOT EXISTS (SELECT 1 FROM referencedata.rights WHERE name = 'STOCK_EVENTS_CANCEL');
