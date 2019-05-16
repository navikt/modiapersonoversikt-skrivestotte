import 'react-app-polyfill/ie11';
import 'react-app-polyfill/stable';
import Application from './application';
import NAVSPA from "./NAVSPA";
import './index.less';

if (process.env.REACT_APP_MOCK === 'true') {
    require('./mock');
}


NAVSPA.eksporter('modiapersonoversikt-skrivestotte', Application);
