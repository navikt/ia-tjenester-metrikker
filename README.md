# ia-tjenester-metrikker

Applikasjon i GCP som lagrer metrikker om IA tjenester.

## Kjøre lokalt
Fra IntelliJ: 
 - sett opp profile til `local` i `run configuration`
 - start `main()` funksjon i `LokalApp.kt`

### Dokumentasjon
API dok finner du her: http://localhost:8080/ia-tjenester-metrikker/swagger-ui/index.html

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
