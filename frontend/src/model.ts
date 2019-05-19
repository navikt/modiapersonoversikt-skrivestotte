export type UUID = string;

export enum Locale {
    nb_NO = 'nb_NO',
    nn_NO = 'nn_NO',
    en_US = 'en_US'
}

export const localeString: { [key in Locale]: string } = {
    nb_NO: 'Bokmål',
    nn_NO: 'Nynorsk',
    en_US: 'Engelsk'
};

export type Tekst = {
    id?: UUID;
    overskrift: string;
    tags: Array<string>;
    innhold: {
        [key in Locale]?: string
    };
}

export type Tekster = {
    [key: string]: Tekst
}

