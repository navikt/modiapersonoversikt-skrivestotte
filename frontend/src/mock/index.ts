import FetchMock, {JSONValue, Middleware, MiddlewareUtils, ResponseUtils} from 'yet-another-fetch-mock';
import {Tekst, Tekster} from "../model";

const loggingMiddleware: Middleware = (request, response) => {
    // tslint:disable
    console.groupCollapsed(`${request.method} ${request.url}`);
    console.groupCollapsed('config');
    console.log('queryParams', request.queryParams);
    console.log('pathParams', request.pathParams);
    console.log('body', request.body);
    console.groupEnd();

    try {
        console.log('response', JSON.parse(response.body));
    } catch (e) {
        console.log('response', response);
    }

    console.groupEnd();
    // tslint:enable
    return response;
};

console.log('============================');
console.log('Using yet-another-fetch-mock');
console.log('============================');
const mock = FetchMock.configure({
    enableFallback: false,
    middleware: MiddlewareUtils.combine(
        MiddlewareUtils.delayMiddleware(500),
        loggingMiddleware
    )
});

const guid = () => Math.random().toString(16).slice(2);

const tekster: Tekster & JSONValue = new Array(50)
    .fill(0)
    .map((_, id) => ({
        id: `id${id}`, overskrift: `Overskrift ${id}`, tags: ['ks', 'arbeid'], innhold: {nb_NO: '', en_US: ''}
    }))
    .reduce((acc, tekst) => ({ ...acc, [tekst.id]: tekst}), {});

mock.get('/skrivestotte', tekster);
mock.put('/skrivestotte', (args) => {
    const tekst = args.body as Tekst & JSONValue;
    if (tekst.id) {
        tekster[tekst.id] = tekst;
        return ResponseUtils.jsonPromise(tekst);
    }
    return Promise.resolve({ status: 400 });
});
mock.post('/skrivestotte', ({ body }) => {
    const id = guid();
    const tekst = body as Tekst & JSONValue;
    tekst.id = id;
    tekster[id] = tekst;
    return tekst;
});
mock.delete('/skrivestotte/:id', ({ pathParams }) => {
    if (tekster[pathParams.id]) {
        delete tekster[pathParams.id];
        return Promise.resolve({ status: 200 });
    } else {
        return Promise.resolve({ status: 400 });
    }
});