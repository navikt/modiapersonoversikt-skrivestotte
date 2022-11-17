import {useQuery, UseQueryResult} from '@tanstack/react-query';
import {FetchError, get} from "../../fetch-utils";
import {Tekster} from "../../model";

const resource = {
    useFetch(): UseQueryResult<Tekster, FetchError> {
        return useQuery({
            queryKey: ['visning'],
            queryFn: () => get<Tekster>('/modiapersonoversikt-skrivestotte/skrivestotte?usageSort=true')
        });
    }
};

export default resource;
