import React from 'react';
import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";
import Header from "./components/header/header";
import Teksterliste from "./components/teksterliste/teksterliste";
import TekstEditor from "./components/tekstereditor/tekstereditor"
import {Tekster} from "./model";
import {useFetch, useFieldState} from "./hooks";
import './application.less';


function Application() {
    const fetchState = useFetch<Tekster>('/skrivestotte');
    const tekster = fetchState.data.withDefault<Tekster>({});

    const sokFS = useFieldState('');
    const checkedFS = useFieldState(Object.keys(tekster)[0] || '');
    const [checked] = checkedFS;
    const checkedTekst = Maybe.of(tekster[checked]);

    return (
        <div className="application">
            <Header/>
            <div className="application__content">
                <Teksterliste tekster={tekster} sok={sokFS} checked={checkedFS}/>
                <div className="application__editor">
                    <TekstEditor tekst={checkedTekst}/>
                </div>
            </div>
        </div>
    );
}

export default Application;
