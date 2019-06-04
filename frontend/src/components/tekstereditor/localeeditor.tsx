import React, {useEffect, useRef} from "react";
import {Locale, localeString} from "../../model";
import AutosizingTextarea from './../autosizing-textarea/autosizing-textarea';
import {FieldState} from "../../hooks/use-field-state";
import {ListState} from "../../hooks/use-list-state";
import {ObjectState} from "../../hooks/use-object-state";

interface Props {
    locale: Locale;
    fieldState: FieldState;
    focusSteal: ObjectState<Locale | null>;
    newLanguage: ListState<string>;
    kanSlettes: boolean;
}

function LocaleEditor(props: Props) {
    const ref = useRef<HTMLTextAreaElement>(null);
    useEffect(() => {
        if (ref.current && props.focusSteal.value === props.locale) {
            ref.current.focus();
            props.focusSteal.setValue(null);
        }
    }, [props.focusSteal, props.focusSteal.value, props.locale]);

    return (
        <div className="localeeditor skjemaelement">
            <label>
                <span className="localeeditor__label skjemaelement__label">
                    {localeString[props.locale]}
                </span>
                <AutosizingTextarea
                    ref={ref}
                    value={props.fieldState.value}
                    onChange={(event) => props.fieldState.onChange(event)}
                    className="skjemaelement__input textarea--medMeta tekstereditor__textarea"
                />
            </label>
            <button
                type="button"
                className="skjemaelement__slett"
                title={`Slett språk: ${localeString[props.locale]}`}
                disabled={!props.kanSlettes}
                onClick={() => {
                    props.fieldState.setValue('');
                    props.newLanguage.remove(props.locale);
                }}
            >
                X
            </button>
        </div>
    );
}

export default LocaleEditor;