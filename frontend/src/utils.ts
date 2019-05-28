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