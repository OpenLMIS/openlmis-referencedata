CREATE TABLE price_changes (
    id UUID PRIMARY KEY,
    programOrderableId UUID,
    price numeric(19,2) NOT NULL,
    authorId uuid NOT NULL,
    occurredDate TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_program_orderable FOREIGN KEY (programOrderableId) REFERENCES program_orderables(id),
    CONSTRAINT fk_author FOREIGN KEY (authorId) REFERENCES users(id)
);