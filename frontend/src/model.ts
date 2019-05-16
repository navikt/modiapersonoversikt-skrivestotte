export type UUID = string;

export enum Locale {
    nb_NO = 'nb_NO',
    nn_NO = 'nn_NO',
    en_US = 'en_US'
}

export interface Tekst {
    id?: UUID;
    overskrift: string;
    tags: Array<string>;
    innhold: {
        [key in Locale]: string
    }
}

