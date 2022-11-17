import React from 'react';
import TagInput from "@navikt/tag-input";
import {Tidsrom} from "../../visning";
import {DetaljertStatistikkTekst} from "../../../../model";
import {tagsQuerySearch} from "../../../../utils";
import StatistikkTabell from "./statistikk-tabell";
import {useFieldState} from "../../../../hooks";
import statistikkResource from './statistikkResource';

interface Props {
    tidsrom: Tidsrom | undefined;
}

function queryParams(tidsrom: Tidsrom | undefined): string {
    if (!tidsrom) {
        return '';
    }
    return `?from=${tidsrom.start}&to=${tidsrom.end}`
}

const matcher = tagsQuerySearch<DetaljertStatistikkTekst>(
    tekst => tekst.tags,
    tekst => [tekst.overskrift, tekst.tags.join('\u0000')],
);
function sokEtterTekster(tekster: Array<DetaljertStatistikkTekst>, query: string): Array<DetaljertStatistikkTekst> {
    return matcher(query, tekster, () => false);
}

function Statistikk(props: Props) {
    const statistikk = statistikkResource.useFetch(queryParams(props.tidsrom), props.tidsrom);
    const data = statistikk.data ?? [];
    const sok = useFieldState('');

    if (statistikk.isLoading) {
        return <div className="center-block statistikk">Laster...</div>
    }

    if (statistikk.isError) {
        return (
            <div className="center-block statistikk">
                <p>
                    `Det skjedde en feil ved lasting av statistikk (${statistikk?.error?.response?.status}).`
                </p>
                <pre>
                    {statistikk?.error?.message}
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
