import React, {Ref} from 'react';
import classNames from 'classnames';
import './autosizing-textarea.scss';

type Props = React.DetailedHTMLProps<React.TextareaHTMLAttributes<HTMLTextAreaElement>, HTMLTextAreaElement>;

function AutosizingTextarea(props: Props, ref: Ref<HTMLTextAreaElement>) {
    const { className, ...other } = props;
    const textareaClassName = classNames("autosizing-textarea__textarea", className);

    return (
        <div className="autosizing-textarea">
            <div className="autosizing-textarea__mirror" aria-hidden={true}>{props.value}</div>
            <textarea
                ref={ref}
                className={textareaClassName}
                {...other}
            />
        </div>
    );
}

export default React.forwardRef<HTMLTextAreaElement, Props>(AutosizingTextarea);
