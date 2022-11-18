import { useQuery, UseQueryResult } from '@tanstack/react-query';
import {DetaljertStatistikk} from "../../../../model";
import {FetchError, get} from "../../../../fetch-utils";
import {Tidsrom} from "../../visning";

function queryKey(queryParams: string): [string, string] {
    return ['statistikk', queryParams];
}
function url(queryParams: string): string {
    return `/modiapersonoversikt-skrivestotte/skrivestotte/statistikk/detaljertbruk${queryParams}`;
}

const resource = {
    useFetch(queryParams: string, tidsrom?: Tidsrom): UseQueryResult<DetaljertStatistikk, FetchError> {
        return useQuery({queryKey: queryKey(queryParams), queryFn: () => get<DetaljertStatistikk>(url(queryParams)), enabled: tidsrom === undefined});
    }
};

export default resource;
