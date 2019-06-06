import React from 'react';
import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";
import Header from "./components/header/header";
import Teksterliste from "./components/teksterliste/teksterliste";
import TekstEditor from "./components/tekstereditor/tekstereditor"
import {Tekst, Tekster} from "./model";
import {useFetch, useFieldState, useObjectState} from "./hooks";
import './application.less';

interface Props {
    renderHead: boolean;
    customDomain?: string;
}

function Application(props: Props) {
    const visEditor = useObjectState<boolean>(false);
    const fetchState = useFetch<Tekster>(`${props.customDomain || ''}/skrivestotte`);
    const tekster = fetchState.data.withDefault<Tekster>({});

    const sokFS = useFieldState('');
    const checked = useFieldState(Object.keys(tekster)[0] || '');
    const checkedTekst = Maybe.of(tekster[checked.value]);
    const skalLeggeTilNy: Maybe<Tekst> = Maybe.of(visEditor.value)
        .filter((value) => value)
        .map(() => ({
            overskrift: '',
            tags: [],
            innhold: {}
        }));

    const visEditorFor = skalLeggeTilNy.or(checkedTekst);

    return (
        <div className="application">
            {props.renderHead && <Header/>}
            <div className="application__content">
                <Teksterliste
                    tekster={tekster}
                    sok={sokFS}
                    checked={checked}
                    visEditor={visEditor}
                />
                <div className="application__editor-wrapper">
                    <TekstEditor
                        key={visEditorFor.map((t) => t.id).withDefault('')}
                        visEditor={visEditor}
                        checked={checked}
                        tekst={visEditorFor}
                        refetch={fetchState.refetch}
                        customDomain={props.customDomain}
                    />
                </div>
            </div>
        </div>
    );
}

export default Application;
