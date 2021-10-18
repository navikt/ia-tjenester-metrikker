update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Oslo'
where substring(kommunenummer, 0, 2) = '03';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Rogaland'
where substring(kommunenummer, 0, 2) = '11';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Møre og Romsdal'
where substring(kommunenummer, 0, 2) = '15';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Nordland'
where substring(kommunenummer, 0, 2) = '18';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Viken'
where substring(kommunenummer, 0, 2) = '30';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Innlandet'
where substring(kommunenummer, 0, 2) = '34';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Vestfold og Telemark'
where substring(kommunenummer, 0, 2) = '38';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Agder'
where substring(kommunenummer, 0, 2) = '42';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Vestland'
where substring(kommunenummer, 0, 2) = '46';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Trøndelag'
where substring(kommunenummer, 0, 2) = '50';

update METRIKKER_IA_TJENESTER_INNLOGGET
set fylke='Troms og Finnmark'
where substring(kommunenummer, 0, 2) = '54';
