import {FieldState, useFieldState} from "../hooks";
import {FormEventHandler} from "react";

export type FormState<T> = {
    [P in keyof T]: string;
}
export type InternalFormState<T> = { [P in keyof T]: FieldState };
export type FormHook<T> = {
    getProps(key: keyof T):FieldState;
    isAllPristine(trim?: boolean): boolean;
    onSubmit(fn: (state: FormState<T>) => void): FormEventHandler<HTMLFormElement>;
}

export default function useFormState<T extends {[key: string]: string}>(initialState: FormState<T>): FormHook<T> {
    const formState: InternalFormState<T> = Object.entries(initialState)
        .map(([key, value]) => ({key, value: useFieldState(value)})) // eslint-disable-line
        .reduce((acc, {key, value}) => ({...acc, [key]: value}), {} as InternalFormState<T>);

    return {
        getProps: (key: keyof T) => formState[key],
        isAllPristine: (trim: boolean = false) => Object.values(formState).every((fieldState) => fieldState.isPristine(trim)),

        onSubmit(fn: (state: FormState<T>) => void): FormEventHandler<HTMLFormElement> {
            return (event) => {
                event.preventDefault();
                const data: FormState<T> = Object.entries(formState)
                    .map(([key, value]) => ({key, value: value.value}))
                    .reduce((acc, {key, value}) => ({...acc, [key]: value}), {} as FormState<T>);

                fn(data);
            }
        }
    };
}