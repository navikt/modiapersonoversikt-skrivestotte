import React from 'react';
import Header from "./components/header/header";
import Teksterliste from "./components/teksterliste/teksterliste";
import {Tekster} from "./model";
import './application.less';

const tekster: Tekster = new Array(50)
    .fill(0)
    .map((_, id) => ({
        id: `id${id}`, overskrift: `Overskrift ${id}`, tags: ['ks', 'arbeid'], innhold: {nb_NO: ''}
    }))
    .reduce((acc, tekst) => ({ ...acc, [tekst.id]: tekst}), {});

function Application() {
    return (
        <div className="application">
            <Header/>
            <div className="application__content">
                <Teksterliste tekster={tekster}/>
                <div className="application__editor">Content here</div>
            </div>
        </div>
    );
}

export default Application;
