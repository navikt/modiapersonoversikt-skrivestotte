# Skrivestøtte for modiapersonoversikt
En tjeneste for administrering av skrivestøtte-tekster i modiapersonoversikt.

## Kjøre lokal
All lokal-kjøring bruker per i dag frontend-mocking eller H2 som database.

### Bare frontend med mocking
1. Kjør `cd frontend && npm run start:mock`

### Frontend og backend med sikkerhet
1. Start `LocalRun.kt`
2. Kjør `cd frontend && npm run start`

Bruker [dev-proxy](https://github.com/navikt/dev-proxy) for sikkerhetsoppsett. Så den må kjøre på `localhost:8080` for at backend skal godta requests.
Bruker in-memory h2 database.

### Frontend og backend uten sikkerhet
1. Start `LocalRunNoSecurity.kt`
2. Kjør `cd frontend && npm run start:nosecurity`

Bruker in-memory h2 database.

### Backend mot ekstern database

1. Start `H2Local.kt`
2. Endre på connection url i `Configuration.kt` (utkommentert connection-url vil koble til lokal instans av h2)


## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan rettes mot:

[Team Personoversikt](https://github.com/navikt/info-team-personoversikt)
