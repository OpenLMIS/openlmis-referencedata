UPDATE programs
    SET enabledatephysicalstockcountcompleted = false WHERE enabledatephysicalstockcountcompleted is null;

ALTER TABLE programs
    ALTER COLUMN enabledatephysicalstockcountcompleted SET NOT NULL;