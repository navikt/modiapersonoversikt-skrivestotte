FROM gcr.io/distroless/java21-debian13:nonroot

WORKDIR /app

COPY build/install/*/lib /lib

ENTRYPOINT ["java", "-cp", "/lib/*", "no.nav.modiapersonoversikt.MainKt"]
