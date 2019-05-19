import React, {ChangeEvent} from 'react';
import {Input, Textarea} from 'nav-frontend-skjema';
import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";
import {Locale, localeString, Tekst} from "../../model";
import {FieldState, useFieldState} from "../../hooks";
import './tekstereditor.less';

interface Props {
    tekst: Maybe<Tekst>;
}

function LocaleEditor(props: { locale: Locale; fieldState: FieldState }) {
    const [value, onChange] = props.fieldState;
    return (
        <Textarea
            label={localeString[props.locale]}
            value={value}
            onChange={(e) => onChange(e as ChangeEvent)}
            tellerTekst={() => null}
        />
    );
}

function getTekst(maybeTekst: Maybe<Tekst>, locale: Locale) {
    return maybeTekst
        .flatMap((tekst) => Maybe.of(tekst.innhold[locale]))
        .withDefault('');
}

function Tekstereditor(props: Props) {
    const bokmal = useFieldState(getTekst(props.tekst, Locale.nb_NO));
    const nynorsk = useFieldState(getTekst(props.tekst, Locale.nn_NO));
    const engelsk = useFieldState(getTekst(props.tekst, Locale.en_US));

    return props.tekst
        .map((tekst) => (
            <>
                <Input label="Overskrift" value={tekst.overskrift}/>
                <LocaleEditor locale={Locale.nb_NO} fieldState={bokmal}/>
                <LocaleEditor locale={Locale.nn_NO} fieldState={nynorsk}/>
                <LocaleEditor locale={Locale.en_US} fieldState={engelsk}/>
            </>
        ))
        .withDefaultLazy(() => <>Ingen tekst valgt</>);
}

export default Tekstereditor;
