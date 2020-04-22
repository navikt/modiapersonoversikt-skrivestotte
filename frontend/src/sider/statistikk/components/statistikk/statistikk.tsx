import React from 'react';
import {Tidsrom} from "../../visning";
import useFetch, {hasError, isPending} from "@nutgaard/use-fetch";
import {DetaljertStatistikk, DetaljertStatistikkTekst} from "../../../../model";
import {toMaybe} from "../../../../utils";
import StatistikkTabell from "./statistikk-tabell";
import {useFieldState} from "../../../../hooks";
import {Input} from "nav-frontend-skjema";

interface Props {
    tidsrom: Tidsrom | undefined;
}

function queryParams(tidsrom: Tidsrom | undefined): string {
    if (!tidsrom) {
        return '';
    }
    return `?from=${tidsrom.start}&to=${tidsrom.end}`
}

const emptyData : DetaljertStatistikk = [];
const fetchOptions = {};

function matcher(sok: string) {
    const fragmenter = sok.toLocaleLowerCase().split(' ');
    return (tekst: DetaljertStatistikkTekst) => {
        const corpus = `${tekst.id} ${tekst.overskrift} ${tekst.tags.join(' ')}`.toLocaleLowerCase();
        return fragmenter.every((fragment) => corpus.includes(fragment));
    }
}

function Statistikk(props: Props) {
    const statistikk = useFetch<DetaljertStatistikk>(`/modiapersonoversikt-skrivestotte/skrivestotte/statistikk/detaljertbruk${queryParams(props.tidsrom)}`, fetchOptions, {
        lazy: props.tidsrom === undefined
    });
    const data = toMaybe(statistikk).withDefault(emptyData);
    const sok = useFieldState('');

    if (isPending(statistikk)) {
        return <div className="center-block statistikk">Laster...</div>
    }

    if (hasError(statistikk)) {
        return (
            <div className="center-block statistikk">
                <p>
                    `Det skjedde en feil ved lasting av statistikk (${statistikk.statusCode}).`
                </p>
                <pre>
                    {statistikk.error}
                </pre>
            </div>
        );
    }

    const tekster = data.filter(matcher(sok.value));
    return (
        <div className="center-block statistikk">
            <Input
                label="Søk"
                className="teksterliste__sok"
                value={sok.value}
                onChange={sok.onChange}
            />
            <StatistikkTabell tidsrom={props.tidsrom!} data={tekster}/>
        </div>
    );
}
export default Statistikk;
