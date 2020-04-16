import {useEffect, useState} from "react";

export interface WindowSize {
    width: number;
    height: number;
}

const isClient = typeof window === 'object';
function getSize() {
    return {
        width: isClient ? window.innerWidth : 0,
        height: isClient ? window.innerHeight : 0
    };
}

export default function useWindowSize(): WindowSize {
    const [windowSize, setWindowSize] = useState(getSize);

    useEffect(() => {
        if (!isClient) {
            return;
        }

        function handleResize() {
            setWindowSize(getSize());
        }

        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, [setWindowSize]);

    return windowSize;
}
