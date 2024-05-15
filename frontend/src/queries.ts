import {
  queryOptions,
  useMutation,
  useQueryClient,
  useSuspenseQuery,
} from "@tanstack/react-query";
import { get, post, put } from "./fetch-utils";
import { Tekst, Tekster } from "./model";
import type { SetOptional } from "type-fest";

export const textsQueryOptions = queryOptions({
  queryKey: ["skrivestotte"],
  queryFn: () => get<Tekster>("/skrivestotte?usageSort=true"),
});

export const useText = (textId: string) => {
  const { data } = useSuspenseQuery(textsQueryOptions);

  return data[textId];
};

export const useMutateText = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (text: SetOptional<Omit<Tekst, "vekttall">, "id">) => {
      const id = text.id;
      if (id)
        return put<Tekst>(`/skrivestotte`, { body: JSON.stringify(text) });

      return post<Tekst>("/skrivestotte", { body: JSON.stringify(text) });
    },
    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: textsQueryOptions.queryKey,
      }),
  });
};
