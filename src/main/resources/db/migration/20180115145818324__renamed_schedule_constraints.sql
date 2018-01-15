ALTER TABLE ONLY processing_schedules
    RENAME CONSTRAINT uk_8hnxrslwj69io4240g3gaogw3 TO unq_processing_schedule_name;

ALTER TABLE ONLY processing_schedules
    RENAME CONSTRAINT uk_dkdb51mfvy91h64s1x6wj3spw TO unq_processing_schedule_code;