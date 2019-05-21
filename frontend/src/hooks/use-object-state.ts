import {Dispatch, SetStateAction, useState} from "react";

export interface ObjectState<T> {
    value: T;
    setValue: Dispatch<SetStateAction<T>>;
}

export default function useObjectState<T>(initialState: T): ObjectState<T> {
    const [value, setValue] = useState(initialState);
    return {
        value,
        setValue
    };
}