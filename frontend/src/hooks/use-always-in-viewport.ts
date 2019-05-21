import React, {DependencyList, useEffect} from "react";

export default function useAlwaysInViewport(selector: string, deps: DependencyList = []) {
    const query = React.useCallback(() => document.querySelector(selector), [selector]);
    useEffect(() => {
        const element = query();
        if (element) {
            element.scrollIntoView({block: 'nearest', inline: 'nearest'});
        }
    }, [...deps, query]); // eslint-disable-line
}