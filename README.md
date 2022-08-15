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

### Koble til preprod-database
Det er mulig å sette opp appen mot preprod-databasen ved å endre på connection-url, og bruke ett personlig `VAULT_TOKEN`
for å koble seg til. **NB!!** Med personlig token har man ikke admin-token, så flyway-migrering må kommenteres ut.   

## Hente brukernavn/passord fra vault
Koble seg til preprod databasen;
1. Start `H2Local.kt`
2. Gå til vault
3. Hent ut connection-url fra `preprod.yml`
4. Hent ut brukernavn/passord vha `vault read postgresql/preprod-fss/creds/modiapersonoversikt-skrivestotte-user`
5. Gå til localhost: http://localhost:8091/ 
6. Bytt "Saved Settings" til "Generic PostgreSQL"
7. Fyll ut connection-url, brukernavn og passord
8. **Connect** 


## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan rettes mot:

[Team Personoversikt](https://github.com/navikt/info-team-personoversikt)
