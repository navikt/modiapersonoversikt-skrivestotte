CREATE TABLE statistikk_raw(
    id          BIGSERIAL   PRIMARY KEY     NOT NULL,
    tidspunkt   TIMESTAMP   DEFAULT NOW()   NOT NULL,
    tekstid     VARCHAR                     NOT NULL
);

CREATE TABLE statistikk(
    id          VARCHAR     PRIMARY KEY     NOT NULL,
    brukt       BIGINT      DEFAULT  0      NOT NULL
);
