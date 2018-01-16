ALTER TABLE processing_schedules DROP CONSTRAINT unq_processing_schedule_code;
CREATE UNIQUE INDEX processing_schedule_code_unique_idx ON processing_schedules (LOWER(code));

ALTER TABLE processing_schedules DROP CONSTRAINT unq_processing_schedule_name;
CREATE UNIQUE INDEX processing_schedule_name_unique_idx ON processing_schedules (LOWER(name));