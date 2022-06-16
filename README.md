# ia-tjenester-metrikker

Applikasjon som mottar og lagrer metrikker fra andre IA-applikasjoner.

Oppdaterer [Datakatalogen](https://data.intern.nav.no/datapakke/76cbb88ca1cd3d335810aec71c587aa1#) med friske tall
daglig (kl. 07:55) via en cron-jobb.

## Kjøre lokalt

Fra IntelliJ, trykk "Edit Configurations", "Add new configuration" og velg `Spring Boot`

- Sett "profile": `local`
- Sett "Main class" til `no.nav.arbeidsgiver.iatjenester.metrikker.LokalApp`

### API-Dokumentasjon

API dok finner du her: http://localhost:8080/ia-tjenester-metrikker/swagger-ui/index.html

### Datapakke til datakatalogen

Datapakker er opprettet i [dev](https://data.dev.intern.nav.no/datapakke/219e562d893afbc8307b5f4e8b210baa#) og
[prod](https://data.intern.nav.no/datapakke/76cbb88ca1cd3d335810aec71c587aa1#). Om det skulle gjøres på nytt, bruk filen
`datapakke-init.json` for å opprette en ny datapakke som følgende (OBS: ta vare på id-en i response):

```
curl -X 'POST' -d @src/test/resources/datapakke-init.json 'https://{utl til datakatalog-api}/v1/datapackage'
```

Endepunktet `ia-tjeneste-metrikker/utsending/datapakke` kan kalles manuelt for å oppdatere Datakatalogen i dev.

### Hente en lokal selvbetjenening-idtoken for å kjøre Postman mot innlogget endepunkt

Start applikasjon og kjør i terminal (Mac med python 2):

`curl --location --request GET 'http://localhost:8080/ia-tjenester-metrikker/local/cookie?issuerId=selvbetjening&audience=aud-localhost&subject=12345678910&cookiename=selvbetjening-idtoken' | python -c "import sys, json; print json.load(sys.stdin)['value']" | tr -d '\n' | pbcopy`

Da er `selvbetjening-token` kopiert i clipboard og kan limes inn direkte etter `Authorization: Bearer ` i f.eks Postman.

### Dette kan du sjekke lokalt

#### Diagnostic endepunkt
I terminal kjør
`curl -v  -X GET "http://localhost:8080/ia-tjenester-metrikker/internal/isalive"`

#### Lokal DB instance
Åpne følgende URL i nettleser: http://localhost:8080/ia-tjenester-metrikker/h2 
 - Driver class: `org.h2.Driver`
 - URL: `jdbc:h2:mem:local-db;MODE=PostgreSQL`
 - user name: `sa`
 - password: _blank_

## GCP konfig
Applikasjon trenger `ALTINN_APIKEY` og `ALTINN_APIGW_APIKEY` for å kunne gjøre oppslag til `altinn-rettigheter-proxy` eller eventuelt direkte til Altinn ved fallback kall.

Disse må legges inn som secrets via `kubectl`. Bruk følgende kommando for å legge dem til `ia-tjenester-metrikker-secrets`: 
```
kubectl create secret generic ia-tjenester-metrikker-secrets \
--from-literal=ALTINN_APIGW_APIKEY=******** \
--from-literal=ALTINN_APIKEY=******* -n arbeidsgiver
```


## Docker
Bygg image
`docker build -t ia-tjenester-metrikker .`

Kjør container (forutsetter .env fil med riktige miljøvariabler)
`docker run --env-file=./.env -d -p 8080:8080 ia-tjenester-metrikker`

## Om autentisering til applikasjonen
Applikasjonen bruker MockOAuth2Server for å lage og verifisere tokens (i både integrasjonstester og ved lokalt kjøring). Den `private_jwk` ble generert [her](https://mkjwk.org/).

# Henvendelser

## For Nav-ansatte
* Dette Git-repositoriet eies av [Team IA i Produktområde arbeidsgiver](https://navno.sharepoint.com/sites/intranett-prosjekter-og-utvikling/SitePages/Produktomr%C3%A5de-arbeidsgiver.aspx).
* Slack-kanaler:
  * [#arbeidsgiver-teamia-utvikling](https://nav-it.slack.com/archives/C016KJA7CFK)
  * [#arbeidsgiver-utvikling](https://nav-it.slack.com/archives/CD4MES6BB)
  * [#arbeidsgiver-general](https://nav-it.slack.com/archives/CCM649PDH)

## For folk utenfor Nav
* Opprett gjerne en issue i Github for alle typer spørsmål
* IT-utviklerne i Github-teamet https://github.com/orgs/navikt/teams/arbeidsgiver
* IT-avdelingen i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)
