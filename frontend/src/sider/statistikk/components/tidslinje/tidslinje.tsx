import React, {MutableRefObject, useEffect, useMemo, useRef, useState} from 'react';
import {StatistikkEntry} from "../../../../model";
import {throttle} from "../../../../utils";
import './tidslinje.scss';
import CoordinateIndex from "./coordinate-index";
import {Tidsrom} from "../../visning";
import useWindowSize from "../../../../hooks/use-window-size";
import {Systemtittel, Element} from "nav-frontend-typografi";
import tidslinjeResource from "./tidslinjeResource";

const graphScale = 4;
const xSize = 720 * graphScale;
const ySize = 60 * graphScale;
const padding = 10 * graphScale;

type PointData = { x: number; y: number; entry: StatistikkEntry }
interface GraphMetadata {
    timeStart: number;
    timeEnd: number;
    timeRange: number;
    timeDelta: number;
    yMax: number;
    yScale: number;
}

function calculateCoordinate(entry: StatistikkEntry, metadata: GraphMetadata): PointData {
    const timeOffset = entry.tidspunkt - metadata.timeStart;
    const x = (padding + (timeOffset / metadata.timeDelta));
    const y = (ySize - (entry.antall * metadata.yScale) - padding);

    return { x, y, entry };
}

function getGraphMetadata(data: Array<StatistikkEntry>): GraphMetadata {
    const timeStart = data[0].tidspunkt;
    const timeEnd = data[data.length - 1].tidspunkt;
    const timeRange = timeEnd - timeStart;
    const timeDelta = timeRange / (xSize - 2 * padding);

    const yMax = data
        .map((entry) => entry.antall)
        .reduce((a, b) => Math.max(a, b), 0);
    const yScale = (ySize - 2 * padding) / yMax;

    return {
        timeStart,
        timeEnd,
        timeRange,
        timeDelta,
        yMax,
        yScale
    };
}

function createGraph(data: Array<StatistikkEntry>): { path: string; hoverSections: Array<PointData>, metadata: GraphMetadata } {
    if (data.length === 0) {
        return { path: '', hoverSections: [], metadata: { timeStart: 0, timeEnd: 1, timeDelta: 1, timeRange: 1, yMax: 1, yScale: 1} };
    }
    const metadata = getGraphMetadata(data);

    return data
        .reduce((acc, entry, index, list) => {
            const { x, y } = calculateCoordinate(entry, metadata);
            acc.path += `L${x.toFixed(2)} ${y.toFixed(2)} `;
            if (index === list.length - 1) {
                acc.path += `L${x.toFixed(2)} ${ySize - padding}`;
            }

            acc.hoverSections.push({ x: +(x / graphScale).toFixed(2), y: entry.antall, entry });
            return acc;
        }, { path: `M${padding} ${ySize - padding}`, hoverSections: [] as Array<PointData>, metadata });
}

function useMouseOver(ref: MutableRefObject<SVGSVGElement | null>, data: Array<PointData>): { isHover: boolean, entry: PointData | undefined } {
    const [isHover, setHover] = useState(false);
    const [entry, setEntry] = useState<PointData | undefined>(undefined);

    const mousein = React.useCallback(() => { setHover(true) }, [setHover]);
    const mouseout = React.useCallback(() => { setHover(false) }, [setHover]);

    useEffect(() => {
        if (ref.current) {
            const element = ref.current;
            element.addEventListener('mouseenter', mousein);
            element.addEventListener('mouseleave', mouseout);

            return () => {
                element.removeEventListener('mouseenter', mousein);
                element.removeEventListener('mouseleave', mouseout);
            };
        }
    }, [mousein, mouseout, ref]);

    useEffect(() => {
        if (ref.current) {
            const element = ref.current;
            const index = new CoordinateIndex(data, (pointData) => pointData.x);

            const handler = throttle((e: MouseEvent) => {
                const { left } = element.getBoundingClientRect();
                const x = e.clientX - left;
                const closest = index.closestTo(x);
                setEntry({ x: e.clientX, y: e.clientY, entry: closest.entry });
            }, 16);

            element.addEventListener('mousemove', handler);
            return () => element.removeEventListener('mousemove', handler);
        }
    }, [ref, setEntry, data]);

    return { isHover, entry };
}

function useSelection(ref: MutableRefObject<SVGSVGElement | null>, data: Array<PointData>, metadata: GraphMetadata) {
    const getInitialSelection = React.useCallback(() => ({ start: metadata.timeStart, end: metadata.timeEnd }), [metadata]);
    const down = useRef(-1);
    const dragging = useRef(false);
    const [selection, setSelection] = useState(getInitialSelection);
    const [workingSelection, setWorkingSelection] = useState<{ start: number; end: number} | undefined>(undefined);

    useEffect(() => {
        if (ref.current) {
            const element = ref.current;
            const { left } = ref.current.getBoundingClientRect();
            const index = new CoordinateIndex(data, (pointData) => pointData.x);

            const downHandler = (e: MouseEvent) => {
                e.preventDefault();
                down.current = e.clientX - left;
                return false;
            };
            const upHandler = (e: MouseEvent) => {
                if (dragging.current) {
                    dragging.current = false;
                    const startTime = index.closestTo(down.current).entry.tidspunkt;
                    const endTime = index.closestTo(e.clientX - left).entry.tidspunkt;

                    if (Math.abs(startTime - endTime) > 1000) {
                        setWorkingSelection(undefined);
                        setSelection({
                            start: Math.min(startTime, endTime),
                            end: Math.max(startTime, endTime)
                        });
                    }
                }
                down.current = -1;
            };
            const clickHandler = () => { setSelection(getInitialSelection); };
            const moveHandler = throttle((e: MouseEvent) => {
                const isDragging = down.current > -1;
                dragging.current = isDragging;

                if (isDragging) {
                    const startTime = index.closestTo(down.current).entry.tidspunkt;
                    const endTime = index.closestTo(e.clientX - left).entry.tidspunkt;

                    if (Math.abs(startTime - endTime) > 1000) {
                        setWorkingSelection({
                            start: Math.min(startTime, endTime),
                            end: Math.max(startTime, endTime)
                        });
                    }
                }
            }, 16);

            element.addEventListener('mousedown', downHandler);
            window.addEventListener('mouseup', upHandler);
            element.addEventListener('dblclick', clickHandler);
            element.addEventListener('mousemove', moveHandler);

            return () => {
                element.removeEventListener('mousedown', downHandler);
                window.removeEventListener('mouseup', upHandler);
                element.removeEventListener('dblclick', clickHandler);
                element.removeEventListener('mousemove', moveHandler);
            }
        }
    }, [ref, data, setSelection, setWorkingSelection, getInitialSelection]);

    const start = calculateCoordinate({ tidspunkt: selection.start, antall: 0 }, metadata);
    const end = calculateCoordinate({ tidspunkt: selection.end, antall: 0 }, metadata);

    let working = null;
    if (workingSelection) {
        const startWorking = calculateCoordinate({ tidspunkt: workingSelection.start, antall: 0 }, metadata);
        const endWorking = calculateCoordinate({ tidspunkt: workingSelection.end, antall: 0 }, metadata);
        working = {
            start: startWorking.x,
            end: endWorking.x,
            width: endWorking.x - startWorking.x
        };
    }

    return {
        start: start.x,
        end: end.x,
        width: end.x - start.x,
        selection,
        working
    };
}

function TidslinjeSvg(props: { data: Array<StatistikkEntry>, onChange: (tidsrom: Tidsrom) => void; }) {
    const data = props.data;
    const windowSize = useWindowSize();
    const graph = useMemo(() => createGraph(data), [data]);
    const svg = useRef<SVGSVGElement>(null);

    const { isHover, entry } = useMouseOver(svg, graph.hoverSections);
    const selection = useSelection(svg, graph.hoverSections, graph.metadata);

    const selectionStart = selection.selection.start;
    const selectionEnd = selection.selection.end;
    const onChange = props.onChange;
    useEffect(() => {
        onChange({ start: selectionStart, end: selectionEnd });
    }, [selectionStart, selectionEnd, onChange]);

    if (data.length === 0) {
        return (
            <>
                <div className="center-block blokk-xxs">
                    <Systemtittel tag="h1">Statistikk</Systemtittel>
                </div>
                <div className="panel center-block blokk-xxs">
                    <Element>Ikke nok data til å generere bruks-graf</Element>
                </div>
            </>
        );
    }

    return (
        <>
            <div className="center-block blokk-xxs">
                <Systemtittel tag="h1">Statistikk</Systemtittel>
            </div>
            <div className="tidslinje center-block blokk-xxs">
                <svg viewBox={`0 0 ${xSize} ${ySize}`} ref={svg}>
                    <path d={graph.path}/>
                    <line x1={padding} y1={ySize - padding} x2={padding} y2={padding} stroke="1" />
                    <line x1={padding} y1={ySize - padding} x2={xSize - padding} y2={ySize - padding} />

                    <rect x={selection.start} y={padding} width={selection.width} height={ySize - 2 * padding} fill="rgba(0, 0, 0, 0.3)"/>
                    { selection.working && <rect x={selection.working.start} y={padding} width={selection.working.width} height={ySize - 2 * padding} fill="rgba(0, 0, 0, 0.3)"/> }
                </svg>
                { (isHover && entry) &&
                    <div className="tidslinje__tooltip" style={{ left: `${Math.min(windowSize.width - 185, entry.x)}px`, top: `${entry.y + 20}px`}}>
                        <span>{new Date(entry.entry.tidspunkt).toLocaleString('nb')}</span>
                        <span>{entry.entry.antall}</span>
                    </div>
                }
                <div className="tidslinje__tidsrom center-block blokk-xxs">
                    <p>
                        <b>Start:</b> {new Date(selection.selection.start).toLocaleString('nb')}
                        <b>Stop:</b> {new Date(selection.selection.end).toLocaleString('nb')}
                    </p>
                </div>
            </div>
        </>
    );
}

interface Props {
    onChange: (tidsrom: Tidsrom) => void;
}

function Tidslinje(props: Props) {
    const statistikk = tidslinjeResource.useFetch();;
    const data: StatistikkEntry[] = statistikk.data ?? [];

    if (statistikk.isLoading) {
        return <>Laster...</>
    }

    if (statistikk.isError) {
        return (
            <>
                <p>
                   `Det skjedde en feil ved lasting av statistikk (${statistikk?.error?.response?.status}).`
                </p>
                <pre>
                    {statistikk?.error?.message}
                </pre>
            </>
        );
    }

    return (
        <TidslinjeSvg data={data} onChange={props.onChange} />
    );
}

export default Tidslinje;
