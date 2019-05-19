import React  from 'react';
import { Input } from 'nav-frontend-skjema';
import { Normaltekst } from 'nav-frontend-typografi';
import classNames from 'classnames';
import {Tekst, Tekster, UUID} from "../../model";
import {cyclicgroup} from "../../utils";
import {FieldState, useForceAllwaysInViewport} from "../../hooks";
import './teksterliste.less';

interface Props {
    tekster: Tekster;
    sok: FieldState;
    checked: FieldState;
}

function TekstListeElement(props: { tekst: Tekst; checked: UUID, onChange: React.ChangeEventHandler }) {
    const cls = classNames('teksterliste__listeelement', {
        'teksterliste__listeelement--checked': props.tekst.id === props.checked
    });
    return (
        <label className={cls}>
            <input
                type="radio"
                name="teksterliste__listeelement"
                value={props.tekst.id}
                checked={props.tekst.id === props.checked}
                onChange={props.onChange}
            />
            <div className="teksterliste__listeelement-content">
                <Normaltekst tag="span">{props.tekst.overskrift}</Normaltekst>
                <Normaltekst tag="span">{props.tekst.tags.join(', ')}</Normaltekst>
            </div>
        </label>
    );
}

function matcherSok(tekst: Tekst, sok: string, checked: UUID) {
    const corpus = `${tekst.overskrift} ${tekst.tags.join(' ')} ${Object.values(tekst.innhold).join(' ')}`;
    return tekst.id === checked || corpus.toLocaleLowerCase().includes(sok.toLocaleLowerCase());
}

function Teksterliste(props: Props) {
    const [sok, setSok] = props.sok;
    const [checked, setChecked, setRawChecked] = props.checked;
    useForceAllwaysInViewport('.teksterliste__listeelement--checked', [checked]);


    const tekster = Object.values(props.tekster).filter((tekst) => matcherSok(tekst, sok, checked));
    const teksterRadios = tekster.map((tekst) => <TekstListeElement tekst={tekst} key={tekst.id} checked={checked} onChange={setChecked} />);

    const keyHandler = (event: React.KeyboardEvent<HTMLInputElement>) => {
        const noModifierKeys = [event.ctrlKey, event.shiftKey, event.altKey, event.metaKey].every((key) => !key);
        if (noModifierKeys && ['ArrowUp', 'ArrowDown'].includes(event.key)) {
            event.preventDefault();
            const direction = event.key === 'ArrowUp' ? -1 : 1;
            const indexOfCurrent = tekster.findIndex((tekst) => tekst.id === checked);
            const newIndex = cyclicgroup(tekster.length, indexOfCurrent + direction);

            setRawChecked(tekster[newIndex].id!);
        }
    };

    return (
        <>
            <Input
                label="Søk"
                className="teksterliste__sok"
                value={sok}
                onChange={setSok}
                onKeyDown={keyHandler}
            />
            <div className="teksterliste__liste">
                {teksterRadios}
            </div>
        </>
    );
}

export default Teksterliste;
