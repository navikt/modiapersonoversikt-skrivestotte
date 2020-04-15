import {Dispatch, SetStateAction, useEffect, useState} from "react";

export enum Page {
    REDIGER = '#rediger',
    STATISTIKK = '#statistikk'
}

function hashToPage(hash: string): Page {
    return hash === Page.STATISTIKK ? Page.STATISTIKK : Page.REDIGER;
}

function useUpdateUrl(page: Page) {
    useEffect(() => {
        window.location.hash = page;
    }, [page]);
}

function useListenToUrlChange(setPage: Dispatch<SetStateAction<Page>>) {
    useEffect(() => {
        const onChange = () => {
            setPage(hashToPage(window.location.hash))
        };

        window.addEventListener('hashchange', onChange);
        return () => window.removeEventListener('hashchange', onChange);
    }, [setPage]);
}

export default function useRouting(): Page {
    const [page, setPage] = useState(hashToPage(window.location.hash));

    useUpdateUrl(page);
    useListenToUrlChange(setPage);

    return page;
}
