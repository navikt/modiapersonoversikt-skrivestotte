import React, {ChangeEvent, RefObject, useCallback, useState, MouseEvent, DragEvent} from "react";
import cls from 'classnames';
import {Flatknapp} from "nav-frontend-knapper";
import './file-upload.scss';

interface Props {
    className?: string;
    file: File | undefined;
    setFile: (file: File | undefined) => void;
}

function Content(props: { file?: File; setFile: (file?: File) => void; input: RefObject<HTMLInputElement> }) {
    const { setFile, input } = props;
    const removeHandler = useCallback((e: MouseEvent) => {
        e.preventDefault();
        if (input.current) {
            input.current.value = '';
        }
        setFile(undefined);
    }, [input, setFile]);

    if (props.file) {
        return (
            <>
                <span>{props.file.name}</span>
                <Flatknapp htmlType="button" onClick={removeHandler}>Fjern</Flatknapp>
            </>
        );
    } else {
        return (
            <>
                <span>Klikk for å velge fil</span>
            </>
        );
    }
}

function FileUpload(props: Props) {
    const inputRef = React.createRef<HTMLInputElement>();
    const [isHighlight, setHighlight] = useState(false);
    const { file, setFile } = props;

    const highlightHandler = useCallback((e: DragEvent) => {
        e.stopPropagation();
        e.preventDefault();
        setHighlight(true);
    }, [setHighlight]);
    const unhighlightHandler = useCallback((e: DragEvent) => {
        e.stopPropagation();
        e.preventDefault();
        setHighlight(false);
    }, [setHighlight]);
    const dropHandler = useCallback((e: DragEvent) => {
        e.stopPropagation();
        e.preventDefault();
        const file: File = e?.dataTransfer?.files.item(0)!!;
        setFile(file);
    }, [setFile]);
    const changeHandler = useCallback((e: ChangeEvent<HTMLInputElement>) => {
        e.stopPropagation();
        e.preventDefault();
        const file: File = e.target?.files?.item(0)!!;
        setFile(file);
    }, [setFile]);


    return (
        <label
            className={cls('file-upload', props.className, { 'file-update--highlight': isHighlight })}
            onDragEnter={highlightHandler}
            onDragOver={highlightHandler}
            onDragLeave={unhighlightHandler}
            onDrop={dropHandler}
        >
            <Content file={file} setFile={setFile} input={inputRef} />
            <input type="file" name="files[]" ref={inputRef} onChange={changeHandler} />
        </label>
    );
}

export default FileUpload;