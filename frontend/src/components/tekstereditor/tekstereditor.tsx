import React, {ChangeEvent} from 'react';
import {Input} from 'nav-frontend-skjema';
import {Fareknapp, Hovedknapp} from 'nav-frontend-knapper';
import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";
import {Locale, localeString, Tekst} from "../../model";
import {FieldState, FormState, ObjectState, useFormState} from "../../hooks";
import './tekstereditor.less';

interface Props {
    visEditor: ObjectState<boolean>;
    checked: FieldState;
    tekst: Maybe<Tekst>;

    refetch(): void;
}

function getTekst(maybeTekst: Maybe<Tekst>, locale: Locale): string {
    return maybeTekst
        .flatMap((tekst) => Maybe.of(tekst.innhold[locale]))
        .withDefault('');
}
const defaultFetchConfig: RequestInit = {
    headers: {
        'Content-Type': 'application/json'
    },
    credentials: 'include'
};

function LocaleEditor(props: { locale: Locale; fieldState: FieldState }) {
    const {value, onChange} = props.fieldState;
    return (
        <div className="skjemaelement">
            <label>
                <span className="skjemaelement__label">{localeString[props.locale]}</span>
                <textarea
                    value={value}
                    onChange={(e) => onChange(e as ChangeEvent)}
                    rows={6}
                    className="skjemaelement__input textarea--medMeta tekstereditor__textarea"
                />
            </label>
        </div>
    );
}

function Tekstereditor(props: Props) {
    const formState = useFormState({
        overskrift: props.tekst.map((tekst) => tekst.overskrift).withDefault(''),
        tags: props.tekst.map((tekst) => tekst.tags.join(' ')).withDefault(''),
        [`${Locale.nb_NO}`]: getTekst(props.tekst, Locale.nb_NO),
        [`${Locale.nn_NO}`]: getTekst(props.tekst, Locale.nn_NO),
        [`${Locale.en_US}`]: getTekst(props.tekst, Locale.en_US)
    });
    const overskrift = formState.getProps('overskrift');
    const tags = formState.getProps('tags');

    return props.tekst
        .map((tekst) => {
            const submitHandler = async (data: FormState<any>) => {
                const {overskrift, tags, ...innhold} = data;
                const method = tekst.id ? 'PUT' : 'POST';
                const body = JSON.stringify({...tekst, overskrift, tags: tags.split(' '), innhold});

                try {
                    const resp = await fetch('/skrivestotte', {...defaultFetchConfig, method, body});
                    const nyTekst = await (resp.json() as Promise<Tekst>);

                    props.refetch();
                    props.checked.setValue(nyTekst.id!);
                    props.visEditor.setValue(false);
                } catch (e) {
                    alert(e)
                }
            };
            const slettHandler = async () => {
                if (window.confirm(`Er du sikker på at du vil slette '${tekst.overskrift}'?`)) {
                    await fetch(`/skrivestotte/${tekst.id}`, {method: 'DELETE'});
                    props.refetch();
                    alert(`'${tekst.overskrift}' slettet...`);
                }
            };

            return (
                <form className="application__editor tekstereditor" onSubmit={formState.onSubmit(submitHandler)}>
                    {props.visEditor.value && <h3>Ny tekst</h3>}
                    <Input label="Overskrift" value={overskrift.value} onChange={overskrift.onChange}/>
                    <Input label="Tags" value={tags.value} onChange={tags.onChange}/>
                    <LocaleEditor locale={Locale.nb_NO} fieldState={formState.getProps(Locale.nb_NO)}/>
                    <LocaleEditor locale={Locale.nn_NO} fieldState={formState.getProps(Locale.nn_NO)}/>
                    <LocaleEditor locale={Locale.en_US} fieldState={formState.getProps(Locale.en_US)}/>

                    <div className="tekstereditor__knapper">
                        <Hovedknapp disabled={formState.isAllPristine(true)}>Lagre</Hovedknapp>
                        <Fareknapp htmlType="button" onClick={slettHandler}>Slett tekst</Fareknapp>
                    </div>
                </form>
            );
        })
        .withDefaultLazy(() => <>Ingen tekst valgt</>);
}

export default Tekstereditor;
