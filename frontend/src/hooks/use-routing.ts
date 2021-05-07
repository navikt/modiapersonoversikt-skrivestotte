import {Dispatch, SetStateAction, useEffect, useState} from "react";

export enum Page {
    REDIGER = '#rediger',
    STATISTIKK = '#statistikk',
    ADMIN = '#admin'
}
const pages = [Page.REDIGER, Page.STATISTIKK, Page.ADMIN];

function hashToPage(hash: string): Page {
    return pages.find((page) => page === hash) ?? Page.REDIGER;
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
