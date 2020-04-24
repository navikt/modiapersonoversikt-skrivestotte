import React from 'react';
import useFetch, {hasError, isPending} from "@nutgaard/use-fetch";
import TagInput from "@navikt/tag-input";
import {Tidsrom} from "../../visning";
import {DetaljertStatistikk, DetaljertStatistikkTekst} from "../../../../model";
import {tagsQuerySearch, toMaybe} from "../../../../utils";
import StatistikkTabell from "./statistikk-tabell";
import {useFieldState} from "../../../../hooks";

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
const matcher = tagsQuerySearch<DetaljertStatistikkTekst>(
    tekst => tekst.tags,
    tekst => [tekst.overskrift, tekst.tags.join('\u0000')],
);
function sokEtterTekster(tekster: Array<DetaljertStatistikkTekst>, query: string): Array<DetaljertStatistikkTekst> {
    return matcher(query, tekster, () => false);
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

    const tekster = sokEtterTekster(data, sok.value);
    return (
        <div className="center-block statistikk">
            <TagInput
                label="Søk"
                name="tag-input-sok"
                className="teksterliste__sok"
                value={sok.value}
                onChange={sok.onChange}
            />
            <StatistikkTabell tidsrom={props.tidsrom!} data={tekster}/>
        </div>
    );
}
export default Statistikk;
