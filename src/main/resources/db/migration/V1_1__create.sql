CREATE TABLE tekst(
    id          VARCHAR     PRIMARY KEY     NOT NULL,
    overskrift  VARCHAR                     NOT NULL,
    tags        VARCHAR                     NOT NULL
);

CREATE TABLE innhold(
    tekst_id    VARCHAR                     NOT NULL,
    locale      VARCHAR                     NOT NULL,
    innhold     VARCHAR                     NOT NULL,
    PRIMARY KEY (tekst_id, locale),
    FOREIGN KEY (tekst_id) REFERENCES tekst(id) ON DELETE CASCADE
);

UPDATE tekst SET overskrift = '', tags = '' WHERE id = ''