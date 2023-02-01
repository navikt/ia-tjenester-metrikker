-- Innlogget metrikker
create table metrikker_ia_tjenester_uinnlogget
(
    id                        serial primary key,
    form_av_tjeneste          varchar   not null,
    kilde_applikasjon         varchar   not null,
    tjeneste_mottakkelsesdato timestamp not null,
    opprettet                 timestamp default current_timestamp
);
