--
-- Name: lots; Type: TABLE; Schema: referencedata; Owner: postgres; Tablespace:
--

CREATE TABLE lots (
    id uuid NOT NULL,
    lotCode text NOT NULL,
    expirationDate timestamp with time zone,
    manufactureDate timestamp with time zone,
    tradeitemid uuid NOT NULL,
    active boolean NOT NULL
);
