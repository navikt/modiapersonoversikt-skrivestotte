import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";
import {Locale, Tekst} from "../../../../model";

export function getTekst(maybeTekst: Maybe<Tekst>, locale: Locale): string {
    return maybeTekst
        .flatMap((tekst) => Maybe.of(tekst.innhold[locale]))
        .map((tekst) => tekst.trim())
        .withDefault('');
}
