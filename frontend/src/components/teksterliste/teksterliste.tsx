import React, {useCallback, useState} from 'react';
import './teksterliste.less';
import {Tekst, Tekster} from "../../model";

interface Props {
    tekster: Tekster
}

function useFieldState(initialState: string | (() => string)): [string, React.ChangeEventHandler] {
    const [state, changeState] = useState(initialState);
    const onChange = useCallback(
        (event: React.ChangeEvent<HTMLInputElement>) => changeState(event.target.value),
        [changeState]
    );

    return [state, onChange]
}

function TekstListeElement(props: { tekst: Tekst }) {
    return (
        <li>
            <span>{props.tekst.overskrift}</span>
            <span>{props.tekst.tags.join(', ')}</span>
        </li>
    );
}

function matcherSok(tekst: Tekst, sok: string) {
    const corpus = `${tekst.overskrift} ${tekst.tags.join(' ')} ${Object.values(tekst.innhold).join(' ')}`;
    return corpus.toLocaleLowerCase().includes(sok.toLocaleLowerCase());
}

function Teksterliste(props: Props) {
    const [sok, setSok] = useFieldState('');

    const tekster = Object.values(props.tekster)
        .filter((tekst) => matcherSok(tekst, sok))
        .map((tekst) => <TekstListeElement tekst={tekst} key={tekst.id}/>);

    return (
        <div className="teksterliste">
            <input type="text" className="teksterliste__sok" value={sok} onChange={setSok}/>
            <ol className="teksterliste__liste">
                {tekster}
            </ol>
        </div>
    );
}

export default Teksterliste;
