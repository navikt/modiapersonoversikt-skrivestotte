interface Slice<T> {
    start: number;
    end: number;
    data: Array<T>;
}

class CoordinateIndex<T> {
    private data: Array<T>;
    private fn: (t: T) => number;

    constructor(data: Array<T>, fn: (t: T) => number) {
        this.data = data.sort((a, b) => fn(a) - fn(b));
        this.fn = fn;
    }

    public closestTo(target: number): T {
        let left = 0;
        let right = this.data.length - 1;

        while (left <= right) {
            const midpoint = Math.floor((left + right) / 2);
            const element = this.data[midpoint];
            const value = this.fn(element);
            if (value < target) {
                left = midpoint + 1;
            } else if (value > target) {
                right = midpoint - 1;
            } else {
                return element;
            }
        }

        const leftElement = this.data[Math.min(left, this.data.length -1)];
        const rightElement = this.data[Math.max(right, 0)];

        const leftDiff = Math.abs(this.fn(leftElement) - target);
        const rightDiff = Math.abs(this.fn(rightElement) - target);

        if (leftDiff <= rightDiff) {
            return leftElement;
        } else {
            return rightElement;
        }
    }
}

export default CoordinateIndex;
