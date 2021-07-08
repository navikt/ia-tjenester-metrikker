# ia-tjenester-metrikker

Applikasjon i GCP som lagrer metrikker om IA tjenester.

## Kjøre lokalt
Fra IntelliJ: 
 - sett opp profile til `local` i `run configuration`
 - start `main()` funksjon i `LokalApp.kt`

### Dokumentasjon
API dok finner du her: http://localhost:8080/ia-tjenester-metrikker/swagger-ui.html

#### Datapakke til datakatalogen
##### Opprettelse av en datapakke

Datapakker er opprettet i dev og prod. Om det skulle gjøres på nytt, bruk filen `datapakke-init.json` for å opprette en ny datapakke som følgende (ta var på id-en i response): 

`curl -X 'POST' -d @src/test/resources/datapakke-init.json 'https://{utl til datakatalog-api}/v1/datapackage'`
##### Cron jobb som sender data til datakatalog
Hver dag kjøres en cron jobb som henter `ìa-tjenester-metrikker` og sender dem til datakatalog kl. 7:55

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
 - URL: `jdbc:h2:mem:local-db`
 - user name: `sa`
 - password: _blank_


# Hvordan fungerer appen
Applikasjonen tar imot _metrikker_ fra andre IA applikasjoner og persistere dem  

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

Kjør container
`docker run -d -p 8080:8080 ia-tjenester-metrikker`


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
