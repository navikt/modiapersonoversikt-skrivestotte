import 'react-app-polyfill/ie11';
import 'react-app-polyfill/stable';
import React from 'react';
import { createRoot } from 'react-dom/client';
import Application from './application';
import {
    QueryClient,
    QueryClientProvider,
} from '@tanstack/react-query'

if (process.env.REACT_APP_MOCK === 'true') {
    require('./mock');
}

const queryClient = new QueryClient()
const root = createRoot(document.getElementById('root')!);
root.render(<QueryClientProvider client={queryClient}>
    <Application renderHead />
</QueryClientProvider>)
