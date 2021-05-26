import React, {FormEvent, useEffect, useState} from 'react';
import {Normaltekst, Systemtittel, Undertittel} from "nav-frontend-typografi";
import { AlertStripeAdvarsel } from 'nav-frontend-alertstriper';
import { Hovedknapp } from "nav-frontend-knapper";
import FileUpload from "./components/file-upload";

async function readContent(file: File): Promise<string> {
    return new Promise<string>((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (event) => {
            resolve(event.target?.result as string)
        }
        reader.onerror = (event) => {
            reject(event);
        }

        reader.readAsText(file);
    });
}


function AdminVisning() {
    const [file, setFile] = useState<File | undefined>(undefined);
    const [error, setError] = useState<string | undefined>(undefined);
    const [content, setContent] = useState<object | undefined>(undefined);
    useEffect(() => {
        if (file === undefined) {
            setContent(undefined);
            setError(undefined);
        } else {
            readContent(file)
                .then((filecontent) => {
                    try {
                        const json = JSON.parse(filecontent);
                        setContent(json)
                    } catch (e) {
                        setError('Ugyldig innhold i filen.')
                    }
                });
        }
    }, [file, setContent, setError]);

    const submitHandler = (event: FormEvent) => {
        event.preventDefault();
    }

    return (
        <div className="admin-visning">
            <section className="center-block blokk-xxs">
                <Systemtittel tag="h1" className="blokk-xs">Admin</Systemtittel>
                <AlertStripeAdvarsel className="blokk-s">
                    Denne siden inneholder operasjoner for synkronisering av data mellom miljø.
                    Bør sådan bare brukes for å synkronisere data fra produksjon til preprod-miljø.
                </AlertStripeAdvarsel>

                <Undertittel className="blokk-xxs">Lag synkroniserings fil</Undertittel>
                <div className="panel blokk-xs">
                    <Normaltekst className="blokk-xxs">
                        Laster ned en JSON-fil med alle tekstene
                    </Normaltekst>
                    <a
                        className="knapp knapp--hoved"
                        href="/modiapersonoversikt-skrivestotte/skrivestotte/download"
                    >
                        Last ned
                    </a>
                </div>

                <Undertittel className="blokk-xxs">Last opp synkroniserings fil</Undertittel>
                <div className="panel">
                    <Normaltekst className="blokk-xxs">
                        Last opp JSON-filen fra foregående steg for å populere databasen.
                    </Normaltekst>
                    <AlertStripeAdvarsel className="blokk-xxs">
                        Dette sletter alle eksisterende tekster. Sjekk derfor om du står i riktig miljø.
                    </AlertStripeAdvarsel>
                    <form onSubmit={submitHandler}>
                        <div className="blokk-xs">
                            <FileUpload file={file} setFile={setFile} />
                            { error && <p className="skjemaelement__feilmelding">{error}</p>}
                            { content && <p>Lastet opp json-fil med {Object.keys(content).length} tekster</p>}
                        </div>
                        <Hovedknapp disabled={file === undefined || error !== undefined}>Last opp</Hovedknapp>
                    </form>
                </div>
            </section>
        </div>
    );
}
export default AdminVisning;