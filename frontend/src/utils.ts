import {parseTekst} from "@navikt/tag-input";

export function cyclicgroup(size: number, value: number): number {
    let v = value % size;
    while (v < 0) { v += size; }
    return v;
}

function prepend(prefix: string) {
    return (value: string) => `${prefix}${value}`
}

export function joinWithPrefix(list: Array<string>) {
    return list
        .map(prepend('#'))
        .join(' ');
}

export function fjernTomtInnhold(obj: { [key: string]: string }):{ [key: string]: string } {
    return Object.entries(obj)
        .filter(([, value]) => value && value.trim().length > 0)
        .reduce((acc, [key, value]) => ({...acc, [key]: value}), {});
}

export function throttle<T extends (...args: any[]) => void>(fn: T, interval: number): T {
    let blocker: number = -1;
    const wrapper = (...args: any[]) => {
        if (blocker === -1) {
            fn(...args);
            blocker = window.setTimeout(() => {
                blocker = -1;
            }, interval);
        }
    };
    return wrapper as T ;
}

export function tagsQuerySearch<T>(getTags: (t: T) => Array<string>, getText: (t: T) => Array<string>): (query: string, data: Array<T>, isSelected: (t: T) => boolean) => Array<T> {
    return (query, data, isSelected) => {
        if (query === '') {
            return data;
        }
        const {tags: queryTags, text} = parseTekst(query);
        const tags = queryTags.map((tag) => tag.toLowerCase());
        const words = text
            .split(' ')
            .map((word) => word.toLowerCase().replace('#', ''))
            .filter((word) => word);

        return data
            .filter((element) => {
                const searchTags = getTags(element).map((tag) => tag.toLowerCase());
                return isSelected(element) || tags.every((tag) => searchTags.includes(tag));
            })
            .filter((element) => {
                const matchtext = getText(element)
                    .join('\u0000')
                    .toLowerCase();

                return isSelected(element) || words.every((word) => matchtext.includes(word));
            });
    }
}
