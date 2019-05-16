import React from 'react';
import Header from "./components/header/header";
import Teksterliste from "./components/teksterliste/teksterliste";
import {Tekster} from "./model";
import './application.less';

const tekster: Tekster = {
    id1: {id: 'id1', overskrift: 'Overskrift 1', tags: ['ks', 'arbeid'], innhold: {nb_NO: ''}},
    id2: {id: 'id2', overskrift: 'Overskrift 2', tags: ['familie'], innhold: {nb_NO: ''}},
    id3: {id: 'id3', overskrift: 'Overskrift 3', tags: ['ks', 'familie'], innhold: {nb_NO: ''}},
    id4: {id: 'id4', overskrift: 'Overskrift 4', tags: ['ks'], innhold: {nb_NO: ''}},
};

function Application() {
    return (
        <div className="application">
            <Header/>
            <div className="application__content">
                <Teksterliste tekster={tekster}/>
            </div>
        </div>
    );
}

export default Application;
