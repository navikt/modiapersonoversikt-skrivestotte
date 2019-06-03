# Skrivestøtte for modiapersonoversikt
En tjeneste for administrering av skrivestøtte-tekster i modiapersonoversikt.

## Kjøre lokal
All lokal-kjøring bruker per i dag frontend-mocking eller en mock av S3.

### Bare frontend med mocking
1. Kjør `cd frontend && npm run start:mock`

### Frontend og backend med sikkerhet
1. Start `LocalRun.kt`
2. Kjør `cd frontend && npm run start`

Bruker [dev-proxy](https://github.com/navikt/dev-proxy) for sikkerhetsoppsett. Så den må kjøre på `localhost:8080` for at backend skal godta requests.

### Frontend og backend uten sikkerhet
1. Start `LocalRunNoSecurity.kt`
2. Kjør `cd frontend && npm run start:nosecurity`


## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan rettes mot:

-   Daniel Winsvold, daniel.winsvold@nav.no
-   Jan-Eirik B. Nævdal, jan.eirik.b.navdal@nav.no
-   Jørund Amsen, jorund.amsen@nav.no
-   Richard Borge, richard.borge@nav.no
-   Nicklas Utgaard, nicklas.utgaard@nav.no
