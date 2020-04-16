import {FetchResult, hasData} from "@nutgaard/use-fetch";
import {MaybeCls as Maybe} from "@nutgaard/maybe-ts";

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

export function toMaybe<TYPE>(fetchresult: FetchResult<TYPE>): Maybe<TYPE> {
    if (hasData(fetchresult)) {
        return Maybe.of(fetchresult.data)
    }
    return Maybe.nothing();
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
