import FetchMock, {JSONValue, Middleware, MiddlewareUtils, ResponseUtils} from 'yet-another-fetch-mock';
import {LocaleValues, Tekst, Tekster} from "../model";

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
const innhold = [
        'A accusantium commodi consequuntur cupiditate delectus dignissimos, doloremque error facilis fugiat impedit nulla odio officiis perspiciatis quae quis repellendus sunt voluptatem? Consectetur.',
        'Adipisci aliquam architecto culpa eaque nulla pariatur quia, voluptates! Accusamus beatae eos expedita facilis, hic provident qui repudiandae vero! Dicta molestiae, totam.',
        'Distinctio esse exercitationem incidunt inventore quidem ratione tenetur. Dolore ex excepturi incidunt molestiae, mollitia perspiciatis provident quibusdam quo soluta, sunt suscipit, voluptas?',
];
function rndInnhold(id: number) {
    const locale = LocaleValues[id % LocaleValues.length];
    return {
        [locale]: `${locale} ${innhold[id % innhold.length]}`
    }
}

const tekster: Tekster & JSONValue = new Array(50)
    .fill(0)
    .map((_, id) => ({
        id: `id${id}`, overskrift: `Overskrift ${id}`, tags: ['ks', 'arbeid'], innhold: {
            ...rndInnhold(id),
            ...rndInnhold(Math.pow(id, 2)),
            ...rndInnhold(Math.pow(id, 3)),
        }
    }))
    .reduce((acc, tekst) => ({ ...acc, [tekst.id]: tekst}), {});

mock.get('/modiapersonoversikt-skrivestotte/skrivestotte', tekster);
mock.put('/modiapersonoversikt-skrivestotte/skrivestotte', (args) => {
    const tekst = args.body as Tekst & JSONValue;
    if (tekst.id) {
        tekster[tekst.id] = tekst;
        return ResponseUtils.jsonPromise(tekst);
    }
    return Promise.resolve({ status: 400 });
});
mock.post('/modiapersonoversikt-skrivestotte/skrivestotte', ({ body }) => {
    const id = guid();
    const tekst = body as Tekst & JSONValue;
    tekst.id = id;
    tekster[id] = tekst;
    return tekst;
});
mock.delete('/modiapersonoversikt-skrivestotte/skrivestotte/:id', ({ pathParams }) => {
    if (tekster[pathParams.id]) {
        delete tekster[pathParams.id];
        return Promise.resolve({ status: 200 });
    } else {
        return Promise.resolve({ status: 400 });
    }
});