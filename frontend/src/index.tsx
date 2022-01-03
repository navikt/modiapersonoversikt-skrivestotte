import 'react-app-polyfill/ie11';
import 'react-app-polyfill/stable';
import * as ReactDOM from "react-dom";
import React from "react";
import Application from './application';

if (process.env.REACT_APP_MOCK === 'true') {
    require('./mock');
}

ReactDOM.render(<Application renderHead />, document.getElementById('root'));
