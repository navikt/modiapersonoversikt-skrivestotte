export function cyclicgroup(size: number, value: number): number {
    let v = value % size;
    while (v < 0) { v += size; }
    return v;
}