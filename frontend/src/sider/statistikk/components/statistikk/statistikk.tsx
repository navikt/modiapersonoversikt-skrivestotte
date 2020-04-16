import React from 'react';
import {Tidsrom} from "../../visning";
import useFetch, {hasError, isPending} from "@nutgaard/use-fetch";
import {DetaljertStatistikk} from "../../../../model";
import {toMaybe} from "../../../../utils";
import StatistikkTabell from "./statistikk-tabell";

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
const fetchOptions = {}

function Statistikk(props: Props) {
    const statistikk = useFetch<DetaljertStatistikk>(`/modiapersonoversikt-skrivestotte/skrivestotte/statistikk/detaljertbruk${queryParams(props.tidsrom)}`, fetchOptions, {
        lazy: props.tidsrom === undefined
    });
    const data = toMaybe(statistikk).withDefault(emptyData);

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

    return (
        <div className="center-block statistikk">
            <StatistikkTabell tidsrom={props.tidsrom!} data={data}/>
        </div>
    );
}
export default Statistikk;
