import React from 'react';
import {DetaljertStatistikk} from "../../../../model";
import {Tidsrom} from "../../visning";
import './statistikk.less';

interface Props {
    tidsrom: Tidsrom;
    data: DetaljertStatistikk;
}

function StatistikkTabell(props: Props) {
    const body = props.data
        .map((element) => (
            <tr key={element.id}>
                <td>{element.overskrift}</td>
                <td>{element.vekttall}</td>
            </tr>
        ));
    return (
        <table className="statistikk__tabell">
            <thead>
            <tr>
                <th>Overskrift</th>
                <th>Antall</th>
            </tr>
            </thead>
            <tbody>
                {body}
            </tbody>
        </table>
    );
}

export default StatistikkTabell;
