import {useQuery, UseQueryResult} from '@tanstack/react-query';
import {StatistikkEntry} from "../../../../model";
import {FetchError, get} from "../../../../fetch-utils";

const resource = {
    useFetch(): UseQueryResult<StatistikkEntry[], FetchError> {
        return useQuery({
            queryKey: ['tidslinje'],
            queryFn: () => get<StatistikkEntry[]>('/modiapersonoversikt-skrivestotte/skrivestotte/statistikk/overordnetbruk')
        });
    }
};

export default resource;
