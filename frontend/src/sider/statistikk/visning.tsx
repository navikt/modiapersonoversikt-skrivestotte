import React, {useState} from 'react';
import Tidslinje from "./components/tidslinje/tidslinje";
import Statistikk from "./components/statistikk/statistikk";
import './visning.scss';

export interface Tidsrom {
    start: number;
    end: number;
}

function StatistikkVisning() {
    const [tidsrom, settTidsrom] = useState<Tidsrom | undefined>(undefined);

    return (
        <div className="statistikk-visning">
            <Tidslinje onChange={settTidsrom}/>
            <Statistikk tidsrom={tidsrom} />
        </div>
    );
}

export default StatistikkVisning;
