-- Innlogget metrikker
create table metrikker_ia_tjenester_innlogget (
  id serial primary key,
  orgn varchar not null,
  naering_kode_5siffer varchar not null,
  form_av_tjeneste varchar not null,
  kilde_applikasjon varchar not null,
  opprettet timestamp default current_timestamp
);
