# ia-tjenester-metrikker

Applikasjon som mottar og lagrer metrikker fra andre IA-applikasjoner.

## Kjøre lokalt i docker-compose

Pass på at docker-compose er satt opp og at docker kjører. Gå så til en rotmappen i prosjektet med en terminal og kjør `./run.sh`

Appen burde da svare på `http://localhost:9090/ia-tjenester-metrikker/ping`

**Resten av guiden går utifra at `./run.sh` kjører, man er på mac og har `jq` og `psql` innstallert** 

For å hente et gyldig token kan du kjøre følgende kommando i en terminal:

```bash
curl -H "Content-Type: application/x-www-form-urlencoded" --request POST  \
  -d grant_type="client_credentials"                                      \
  -d client_id="localhost:arbeidsgiver:ia-tjenester-metrikker"            \
  -d client_secret="client_secret"                                        \
  "http://localhost:6969/tokenx/token" | jq -r '.access_token' | pbcopy
```

Dette henter ut token fra mock-oauth2-server instansen, parses json-responsen og kopierer access tokenet inn i clipboardet.

Man skal så kunne teste å sende inn en metrikk med hjelp av denne kommandoen:

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(pbpaste)" \
  -d '{"orgnr": "811076112", "type": "DIGITAL_IA_TJENESTE", "kilde": "FOREBYGGE_FRAVÆR"}' \
  "http://localhost:9090/ia-tjenester-metrikker/innlogget/mottatt-iatjeneste"
```

Dette burde returnere `{"status":"created"}`. For å sjekke at det har blitt peristert i databasen, sjekker man postgres:

```bash
PGPASSWORD=test psql -h localhost -p 5432 -U postgres postgres -c 'select * from metrikker_ia_tjenester_innlogget'
```
(se at siste rad har timestamp ca når man sendte inn metrikken)

## Kjøre lokalt

Fra IntelliJ, trykk "Edit Configurations", "Add new configuration" og velg `Spring Boot`

- Sett "profile": `local`
- Sett "Main class" til `no.nav.arbeidsgiver.iatjenester.metrikker.LokalApp`

### API-Dokumentasjon

API dok finner du her: http://localhost:8080/ia-tjenester-metrikker/swagger-ui/index.html

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

---

# Kontakt

* For spørsmål eller henvendelser, opprett gjerne et issue her på GitHub.
* Koden utvikles og driftes av Team IA i [Produktområde arbeidsgiver](https://navno.sharepoint.com/sites/intranett-prosjekter-og-utvikling/SitePages/Produktomr%C3%A5de-arbeidsgiver.aspx).
* Slack-kanal [#team-pia](https://nav-it.slack.com/archives/C02DL347ZT2)
