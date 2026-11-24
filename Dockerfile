FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27

WORKDIR /app

COPY build/install/*/lib /lib

CMD ["-cp", "/lib/*", "no.nav.modiapersonoversikt.MainKt"]
