import React from 'react';
import {Element, Normaltekst} from 'nav-frontend-typografi';
import {Knapp} from "nav-frontend-knapper";
import TagInput from "@navikt/tag-input";
import classNames from 'classnames';
import {Tekst, Tekster, UUID} from "../../../../model";
import {cyclicgroup, joinWithPrefix, tagsQuerySearch} from "../../../../utils";
import {FieldState, ObjectState, useForceAllwaysInViewport} from "../../../../hooks";
import './teksterliste.less';

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
                <Element className="teksterliste__overskrift">{props.tekst.overskrift}</Element>
                <Normaltekst className="teksterliste__tags">{joinWithPrefix(props.tekst.tags)}</Normaltekst>
            </div>
        </label>
    );
}

const matcher = tagsQuerySearch<Tekst>(
    tekst => tekst.tags,
    tekst => [tekst.overskrift, Object.values(tekst.innhold).join('\u0000'), tekst.tags.join('\u0000')],
);
function sokEtterTekster(tekster: Array<Tekst>, query: string, valgtTekst: UUID): Array<Tekst> {
    return matcher(query, tekster, (tekst) => tekst.id === valgtTekst);
}

function Teksterliste(props: Props) {
    useForceAllwaysInViewport('.teksterliste__listeelement--checked', [props.checked.value]);

    const tekster = sokEtterTekster(Object.values(props.tekster), props.sok.value, props.checked.value);

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
            <TagInput
                label="Søk"
                name="tag-input-sok"
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
