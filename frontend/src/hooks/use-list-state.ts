import {Dispatch, SetStateAction, useState} from "react";

export interface ListState<T> {
    value: Array<T>;
    setValue: Dispatch<SetStateAction<Array<T>>>;
    push(t: T): void;
    remove(t: T): void;
}

export default function useListState<T>(initialState: Array<T>): ListState<T> {
    const [value, setValue] = useState(initialState);

    const push = (t: T) => setValue([...value, t]);
    const remove = (t: T) => setValue(value.filter((v) => v !== t));

    return {
        value,
        setValue,
        push,
        remove
    };
}