import {
  queryOptions,
  useMutation,
  useQueryClient,
  useSuspenseQuery,
} from "@tanstack/react-query";
import { del, get, post, put } from "./fetch-utils";
import { Tekst, Tekster } from "./model";
import type { SetOptional } from "type-fest";

const basePath = import.meta.env.BASE_URL.replace(/^\/$/, "");

export const textsQueryOptions = queryOptions({
  queryKey: ["skrivestotte"],
  queryFn: () => get<Tekster>(`${basePath}/skrivestotte?usageSort=true`),
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
        return put<Tekst>(`${basePath}/skrivestotte`, {
          body: JSON.stringify(text),
        });

      return post<Tekst>(`${basePath}/skrivestotte`, {
        body: JSON.stringify(text),
      });
    },
    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: textsQueryOptions.queryKey,
      }),
  });
};

export const useDeleteText = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: Tekst["id"]) => {
      return del(`${basePath}/skrivestotte/${id}`);
    },
    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: textsQueryOptions.queryKey,
      }),
  });
};
