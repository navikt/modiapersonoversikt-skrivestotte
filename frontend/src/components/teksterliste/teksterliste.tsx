import React from 'react';
import {Input} from 'nav-frontend-skjema';
import {Element, Normaltekst} from 'nav-frontend-typografi';
import classNames from 'classnames';
import {Tekst, Tekster, UUID} from "../../model";
import {cyclicgroup, joinWithPrefix} from "../../utils";
import {FieldState, ObjectState, useForceAllwaysInViewport} from "../../hooks";
import './teksterliste.less';
import {Knapp} from "nav-frontend-knapper";

interface Props {
    tekster: Tekster;
    sok: FieldState;
    checked: FieldState;
    visEditor: ObjectState<boolean>;
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
                <Element>{props.tekst.overskrift}</Element>
                <Normaltekst>{joinWithPrefix(props.tekst.tags)}</Normaltekst>
            </div>
        </label>
    );
}

function matcher(sok: string, checked: UUID) {
    return (tekst: Tekst) => {
        const corpus = `${tekst.overskrift} ${tekst.tags.join(' ')} ${Object.values(tekst.innhold).join(' ')}`;
        return tekst.id === checked || corpus.toLocaleLowerCase().includes(sok.toLocaleLowerCase());
    }
}

function Teksterliste(props: Props) {
    useForceAllwaysInViewport('.teksterliste__listeelement--checked', [props.checked.value]);

    const tekster = Object.values(props.tekster).filter(matcher(props.sok.value, props.checked.value));

    const changeHandler = (event: React.ChangeEvent) => {
        props.checked.onChange(event);
        props.visEditor.setValue(false);
    };

    const keyHandler = (event: React.KeyboardEvent<HTMLInputElement>) => {
        const noModifierKeys = [event.ctrlKey, event.shiftKey, event.altKey, event.metaKey].every((key) => !key);
        if (noModifierKeys && ['ArrowUp', 'ArrowDown'].includes(event.key)) {
            event.preventDefault();
            const direction = event.key === 'ArrowUp' ? -1 : 1;
            const indexOfCurrent = tekster.findIndex((tekst) => tekst.id === props.checked.value);
            const newIndex = cyclicgroup(tekster.length, indexOfCurrent + direction);

            props.checked.setValue(tekster[newIndex].id!);
            props.visEditor.setValue(false);
        }
    };

    const leggTilNyHandler = () => {
        props.checked.setValue('');
        props.visEditor.setValue(true);
    };

    return (
        <>
            <Input
                label="Søk"
                className="teksterliste__sok"
                value={props.sok.value}
                onChange={props.sok.onChange}
                onKeyDown={keyHandler}
            />
            <div className="teksterliste__leggtilny">
                <Knapp mini onClick={leggTilNyHandler}>
                    Legg til ny
                </Knapp>
            </div>
            <div className="teksterliste__liste">
                {tekster.map((tekst) => (
                    <TekstListeElement
                        key={tekst.id}
                        tekst={tekst}
                        checked={props.checked.value}
                        onChange={changeHandler}
                    />
                ))}
            </div>
        </>
    );
}

export default Teksterliste;
