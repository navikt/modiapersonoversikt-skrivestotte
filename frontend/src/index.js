import 'babel-polyfill';
import 'whatwg-fetch';

import React from 'react';
import ReactDOM from 'react-dom';
import Application from './application';
import './index.css';

ReactDOM.render(<Application />, document.getElementById('root'));
