import React from 'react';
import {Input} from 'nav-frontend-skjema';
import {Fareknapp, Hovedknapp, Knapp} from 'nav-frontend-knapper';
import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";
import {Locale, localeString, LocaleValues, Tekst} from "../../model";
import {
    FieldState,
    FormState,
    ObjectState,
    useFormState,
    useListState,
    useObjectState
} from "../../hooks";
import {fjernTomtInnhold} from "../../utils";
import * as Fetcher from './fetch-utils';
import LocaleEditor from "./localeeditor";
import './tekstereditor.less';
import {getTekst} from "./utils";
import TekstidOgBruk from "./tekstid-og-bruk";

interface Props {
    visEditor: ObjectState<boolean>;
    checked: FieldState;
    tekst: Maybe<Tekst>;

    refetch(): void;
}

function onSubmit(tekst: Tekst, lagrer: ObjectState<boolean>, props: Props) {
    return async (data: FormState<any>) => {
        const {overskrift, tags, ...innhold} = data;
        const lagreTekst = tekst.id ? Fetcher.put : Fetcher.post;
        const body = JSON.stringify({
            ...tekst,
            overskrift,
            tags: tags.split(' '),
            innhold: fjernTomtInnhold(innhold)
        });

        try {
            lagrer.setValue(true);
            const nyTekst = await lagreTekst<Tekst>('/modiapersonoversikt-skrivestotte/skrivestotte', {body});

            props.refetch();
            props.checked.setValue(nyTekst.id!);
            props.visEditor.setValue(false);
            lagrer.setValue(false);
        } catch (e) {
            window.alert(e);
        }
    }
}
function onDelete(tekst: Tekst, props: Props) {
    return async () => {
        if (window.confirm(`Er du sikker på at du vil slette '${tekst.overskrift}'?`)) {
            await Fetcher.del(`/modiapersonoversikt-skrivestotte/skrivestotte/${tekst.id}`);
            props.refetch();
            window.alert(`'${tekst.overskrift}' slettet...`);
        }
    }
}

function Tekstereditor(props: Props) {
    const newLanguage = useListState<string>([]);
    const focusSteal = useObjectState<Locale | null>(null);
    const lagrer = useObjectState<boolean>(false);
    const formState = useFormState({
        overskrift: props.tekst.map((tekst) => tekst.overskrift).withDefault(''),
        tags: props.tekst.map((tekst) => tekst.tags.join(' ')).withDefault(''),
        ...LocaleValues.reduce((acc, locale) => ({
            ...acc,
            [locale]: getTekst(props.tekst, locale)
        }), {})
    });

    return props.tekst
        .map((tekst) => {
            const overskrift = formState.getProps('overskrift');
            const tags = formState.getProps('tags');

            const submitHandler = onSubmit(tekst, lagrer, props);
            const slettHandler = onDelete(tekst, props);

            const localesMedEditor = LocaleValues
                .filter((locale) => {
                    const hasValue = formState.getProps(locale).value.trim().length > 0;
                    const isNewlyAdded = newLanguage.value.includes(locale);
                    return hasValue || isNewlyAdded;
                });

            const localesMedInnhold = localesMedEditor
                .filter((locale) => formState.getProps(locale).value.trim().length > 0)
                .length;

            const disableLagring = localesMedInnhold === 0 || formState.isAllPristine(true);

            const editors = localesMedEditor
                .map((locale) => (
                    <LocaleEditor
                        key={locale}
                        locale={locale}
                        fieldState={formState.getProps(locale)}
                        focusSteal={focusSteal}
                        newLanguage={newLanguage}
                        kanSlettes={localesMedEditor.length > 1}
                    />
                ));

            const localesSomKanLeggesTil = LocaleValues
                .filter((locale) => !localesMedEditor.includes(locale))
                .map((locale) => (
                    <Knapp
                        mini
                        key={locale}
                        htmlType="button"
                        onClick={() => {
                            newLanguage.push(locale);
                            focusSteal.setValue(locale);
                        }}
                    >
                        Legg til {localeString[locale]}
                    </Knapp>
                ));

            return (
                <form className="application__editor tekstereditor" onSubmit={formState.onSubmit(submitHandler)}>
                    {props.visEditor.value && <h3>Ny tekst</h3>}
                    <TekstidOgBruk vis={!props.visEditor.value} tekstId={tekst.id} vekttall={tekst.vekttall}/>

                    <Input label="Overskrift" value={overskrift.value} onChange={overskrift.onChange}/>
                    <Input label="Tags, skill med mellomrom:" value={tags.value} onChange={tags.onChange}/>

                    {editors}

                    <div className="tekstereditor__ekstrasprak">
                        {localesSomKanLeggesTil}
                    </div>
                    <div className="tekstereditor__knapper">
                        <Hovedknapp disabled={disableLagring} spinner={lagrer.value} autoDisableVedSpinner>
                            Lagre
                        </Hovedknapp>
                        <Fareknapp htmlType="button" onClick={slettHandler}>Slett</Fareknapp>
                    </div>
                </form>
            );
        })
        .withDefaultLazy(() => <>Ingen tekst valgt</>);
}

export default Tekstereditor;
