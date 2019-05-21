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