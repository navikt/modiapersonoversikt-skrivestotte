import { http, HttpResponse } from "msw";
import { LocaleValues, Tekst, Tekster } from "../model";
import overordnetStatistikk from "./overordnetstatistikk";

const guid = () => Math.random().toString(16).slice(2);
const innhold = [
  "A accusantium commodi consequuntur cupiditate delectus dignissimos, doloremque error facilis fugiat impedit nulla odio officiis perspiciatis quae quis repellendus sunt voluptatem? Consectetur.",
  "Adipisci aliquam architecto culpa eaque nulla pariatur quia, voluptates! Accusamus beatae eos expedita facilis, hic provident qui repudiandae vero! Dicta molestiae, totam.",
  "Distinctio esse exercitationem incidunt inventore quidem ratione tenetur. Dolore ex excepturi incidunt molestiae, mollitia perspiciatis provident quibusdam quo soluta, sunt suscipit, voluptas?",
];
function rndInnhold(id: number) {
  const locale = LocaleValues[id % LocaleValues.length];
  return {
    [locale]: `${locale} ${innhold[id % innhold.length]}`,
  };
}

const tekster: Tekster = new Array(50)
  .fill(0)
  .map((_, id) => ({
    id: `id${id}`,
    overskrift: `Overskrift ${id}`,
    tags: ["ks", "arbeid"],
    innhold: {
      ...rndInnhold(id),
      ...rndInnhold(Math.pow(id, 2)),
      ...rndInnhold(Math.pow(id, 3)),
    },
    vekttall: id,
  }))
  .reduce((acc, tekst) => ({ ...acc, [tekst.id]: tekst }), {});

export const handlers = [
  http.get("/skrivestotte", () => HttpResponse.json(tekster)),

  http.put("/skrivestotte", async ({ request }) => {
    const tekst = (await request.json()) as Tekst;
    if (tekst.id) {
      tekster[tekst.id] = tekst;
      return HttpResponse.json(tekst);
    }
    return new HttpResponse(null, { status: 400 });
  }),

  http.post("/skrivestotte", async ({ request }) => {
    const id = guid();
    const tekst = (await request.json()) as Tekst;
    tekst.id = id;
    tekster[id] = tekst;
    return HttpResponse.json(tekst);
  }),

  http.delete("/skrivestotte/:id", ({ params }) => {
    const { id } = params as { id: string };
    if (tekster[id]) {
      delete tekster[id];
      return new HttpResponse(null, { status: 200 });
    } else {
      return new HttpResponse(null, { status: 400 });
    }
  }),

  http.post("/skrivestotte/upload", async ({ request }) => {
    const tekster = await request.json();
    return HttpResponse.json(tekster);
  }),

  http.get("/skrivestotte/statistikk/overordnetbruk", () =>
    HttpResponse.json(overordnetStatistikk),
  ),

  http.get("/skrivestotte/statistikk/detaljertbruk", () =>
    HttpResponse.json(Object.values(tekster)),
  ),
];
