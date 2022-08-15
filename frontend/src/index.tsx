import 'react-app-polyfill/ie11';
import 'react-app-polyfill/stable';
import React from 'react';
import { createRoot } from 'react-dom/client';
import Application from './application';

if (process.env.REACT_APP_MOCK === 'true') {
    require('./mock');
}
const root = createRoot(document.getElementById('root')!);
root.render(<Application renderHead />)
