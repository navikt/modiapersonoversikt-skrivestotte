import {Input} from "nav-frontend-skjema";
import React from "react";

interface Props {
    vis: boolean;
    tekstId?: string;
    vekttall: number;
}

function TekstidOgBruk(props: Props) {
    if (!props.vis) {
        return null
    }

    return (
        <div className="tekstereditor__tekstid-og-bruk">
            <Input label="Tekst-id" defaultValue={props.tekstId} className="input--disabled" aria-disabled="true" readOnly />
            <Input label="Vekttall" defaultValue={''+props.vekttall} className="input--disabled" aria-disabled="true" readOnly />
        </div>
    );
}

export default TekstidOgBruk;
