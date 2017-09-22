CREATE TABLE ideal_stock_amounts (
    id uuid NOT NULL,
    facilityid uuid NOT NULL,
    programid uuid NOT NULL,
    orderableid uuid NOT NULL,
    processingperiodid uuid NOT NULL,
    amount integer NOT NULL
);