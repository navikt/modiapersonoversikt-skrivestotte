import {Dispatch, SetStateAction, useMemo, useState} from "react";

export interface ObjectState<T> {
    value: T;
    setValue: Dispatch<SetStateAction<T>>;
}

export default function useObjectState<T>(initialState: T): ObjectState<T> {
    const [value, setValue] = useState(initialState);
    return useMemo(() => ({
        value,
        setValue
    }), [value, setValue]);
}