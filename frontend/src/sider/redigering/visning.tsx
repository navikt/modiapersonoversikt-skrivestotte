import React from 'react';
import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";
import Teksterliste from "./components/teksterliste/teksterliste";
import TekstEditor from "./components/tekstereditor/tekstereditor";
import {useFieldState, useObjectState} from "../../hooks";
import {Tekst, Tekster} from "../../model";
import './visning.scss';
import visningResource from "./visningResource";

function RedigerVisning() {
    const visEditor = useObjectState<boolean>(false);
    const fetchState = visningResource.useFetch();
    const tekster: Tekster = fetchState.data ?? {};

    const sokFS = useFieldState('');
    const checked = useFieldState(Object.keys(tekster)[0] || '');
    const checkedTekst = Maybe.of(tekster[checked.value]);
    const skalLeggeTilNy: Maybe<Tekst> = Maybe.of(visEditor.value)
        .filter((value) => value)
        .map(() => ({
            overskrift: '',
            tags: [],
            innhold: {},
            vekttall: 0
        }));

    const visEditorFor = skalLeggeTilNy.or(checkedTekst);

    return (
        <>
            <Teksterliste
                tekster={tekster}
                sok={sokFS}
                checked={checked}
                visEditor={visEditor}
            />
            <div className="rediger-page__editor-wrapper">
                <TekstEditor
                    key={visEditorFor.map((t) => t.id).withDefault('')}
                    visEditor={visEditor}
                    checked={checked}
                    tekst={visEditorFor}
                    refetch={fetchState.refetch}
                />
            </div>
        </>
    );
}

export default RedigerVisning;
