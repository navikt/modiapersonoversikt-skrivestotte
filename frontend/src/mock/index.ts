import FetchMock, {MiddlewareUtils} from 'yet-another-fetch-mock';
import {LocaleValues, Tekst, Tekster} from "../model";
import overordnetStatistikk from "./overordnetstatistikk";

console.log('============================');
console.log('Using yet-another-fetch-mock');
console.log('============================');
const mock = FetchMock.configure({
    enableFallback: false,
    middleware: MiddlewareUtils.combine(
        MiddlewareUtils.delayMiddleware(500),
        MiddlewareUtils.loggingMiddleware()
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

const tekster: Tekster = new Array(50)
    .fill(0)
    .map((_, id) => ({
        id: `id${id}`,
        overskrift: `Overskrift ${id}`,
        tags: ['ks', 'arbeid'],
        innhold: {
            ...rndInnhold(id),
            ...rndInnhold(Math.pow(id, 2)),
            ...rndInnhold(Math.pow(id, 3)),
        },
        vekttall: id
    }))
    .reduce((acc, tekst) => ({ ...acc, [tekst.id]: tekst}), {});

mock.get('/modiapersonoversikt-skrivestotte/skrivestotte', (req, resp, ctx) => resp(ctx.json(tekster)));
mock.put('/modiapersonoversikt-skrivestotte/skrivestotte', (req, resp, ctx) => {
    const tekst = req.body as Tekst;
    if (tekst.id) {
        tekster[tekst.id] = tekst;
        return resp(ctx.json(tekst));
    }
    return resp(ctx.status(400));
});
mock.post('/modiapersonoversikt-skrivestotte/skrivestotte', (req, resp, ctx) => {
    const id = guid();
    const tekst = req.body as Tekst;
    tekst.id = id;
    tekster[id] = tekst;
    return resp(ctx.json(tekst));
});
mock.delete('/modiapersonoversikt-skrivestotte/skrivestotte/:id', (req, resp, ctx) => {
    if (tekster[req.pathParams.id]) {
        delete tekster[req.pathParams.id];
        return resp(ctx.status(200));
    } else {
        return resp(ctx.status(400));
    }
});

mock.get('/modiapersonoversikt-skrivestotte/skrivestotte/statistikk/overordnetbruk', (req, resp, ctx) => resp(ctx.json(overordnetStatistikk)));
mock.get('/modiapersonoversikt-skrivestotte/skrivestotte/statistikk/detaljertbruk', (req, resp, ctx) => resp(ctx.json(Object.values(tekster))));
