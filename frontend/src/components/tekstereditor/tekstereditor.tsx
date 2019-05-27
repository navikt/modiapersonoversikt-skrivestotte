import React, {ChangeEvent} from 'react';
import {Input, Select} from 'nav-frontend-skjema';
import {Fareknapp, Hovedknapp, Knapp} from 'nav-frontend-knapper';
import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";
import {Locale, localeString, LocaleValues, Tekst} from "../../model";
import {FieldState, FormState, ListState, ObjectState, useFieldState, useFormState, useListState} from "../../hooks";
import './tekstereditor.less';
import {fjernTomtInnhold} from "../../utils";

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

function LocaleEditor(props: { locale: Locale; fieldState: FieldState, newLanguage: ListState<string> }) {
    return (
        <div className="skjemaelement">
            <label>
                <span className="skjemaelement__label">
                    <button
                        type="button"
                        className="skjemaelement__slett"
                        title={`Slett språk: ${localeString[props.locale]}`}
                        onClick={() => {
                            props.fieldState.setValue('');
                            props.newLanguage.remove(props.locale);
                        }}
                    >
                        X
                    </button>
                    {localeString[props.locale]}
                </span>
                <textarea
                    value={props.fieldState.value}
                    onChange={(e) => props.fieldState.onChange(e as ChangeEvent)}
                    rows={6}
                    className="skjemaelement__input textarea--medMeta tekstereditor__textarea"
                />
            </label>
        </div>
    );
}

function Tekstereditor(props: Props) {
    const newLanguage = useListState<string>([]);
    const leggTil = useFieldState('');
    const formState = useFormState({
        overskrift: props.tekst.map((tekst) => tekst.overskrift).withDefault(''),
        tags: props.tekst.map((tekst) => tekst.tags.join(' ')).withDefault(''),
        ...LocaleValues.reduce((acc, locale) => ({
            ...acc,
            [locale]: getTekst(props.tekst, locale)
        }), {})
    });

    const overskrift = formState.getProps('overskrift');
    const tags = formState.getProps('tags');

    return props.tekst
        .map((tekst) => {
            const submitHandler = async (data: FormState<any>) => {
                const {overskrift, tags, ...innhold} = data;
                const method = tekst.id ? 'PUT' : 'POST';
                const body = JSON.stringify({...tekst, overskrift, tags: tags.split(' '), innhold: fjernTomtInnhold(innhold)});

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

            const localesMedEditor = LocaleValues
                .filter((locale) => {
                    const hasValue = formState.getProps(locale).value.trim().length > 0;
                    const isNewlyAdded = newLanguage.value.includes(locale);
                    return hasValue || isNewlyAdded;
                });

            const localesEditor = localesMedEditor
                .map((locale) => (
                    <LocaleEditor
                        key={locale}
                        locale={locale}
                        fieldState={formState.getProps(locale)}
                        newLanguage={newLanguage}
                    />
                ));
            const localesSomKanLeggesTil = LocaleValues
                .filter((locale) => !localesMedEditor.includes(locale))
                .map((locale) => (
                    <option key={locale} value={locale}>{localeString[locale]}</option>
                ));

            return (
                <form className="application__editor tekstereditor" onSubmit={formState.onSubmit(submitHandler)}>
                    {props.visEditor.value && <h3>Ny tekst</h3>}
                    <Input label="Overskrift" value={overskrift.value} onChange={overskrift.onChange}/>
                    <Input label="Tags" value={tags.value} onChange={tags.onChange}/>

                    {localesEditor}

                    <div className="tekstereditor__knapper">
                        <Hovedknapp disabled={formState.isAllPristine(true)}>Lagre</Hovedknapp>
                        <div>
                            <Select label="Legg til språk" value={leggTil.value} onChange={leggTil.onChange}>
                                <option value="">Velg</option>
                                {localesSomKanLeggesTil}
                            </Select>
                            <Knapp
                                htmlType="button"
                                disabled={leggTil.value === ''}
                                onClick={() => {
                                    newLanguage.push(leggTil.value);
                                    leggTil.setValue('')
                                }}
                            >
                                Legg til
                            </Knapp>
                        </div>
                        <Fareknapp htmlType="button" onClick={slettHandler}>Slett alle</Fareknapp>
                    </div>
                </form>
            );
        })
        .withDefaultLazy(() => <>Ingen tekst valgt</>);
}

export default Tekstereditor;
