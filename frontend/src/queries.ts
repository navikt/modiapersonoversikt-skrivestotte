import {
  queryOptions,
  useMutation,
  useQueryClient,
  useSuspenseQuery,
} from "@tanstack/react-query";
import { del, get, post, put } from "./fetch-utils";
import { Tekst, Tekster } from "./model";
import type { SetOptional } from "type-fest";
import { toast } from "react-toastify";

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
      if (id) {
        const updatePromise = put<Tekst>(`${basePath}/skrivestotte`, {
          body: JSON.stringify(text),
        });

        toast.promise(updatePromise, {
          pending: "Lagrer tekst...",
          success: "Tekst lagret",
          error: "Klarte ikke å lagre teksten",
        });

        return updatePromise;
      }

      const createPromise = post<Tekst>(`${basePath}/skrivestotte`, {
        body: JSON.stringify(text),
      });
      toast.promise(createPromise, {
        pending: "Lagrer tekst...",
        success: "Tekst lagret",
        error: "Klarte ikke å lagre teksten",
      });

      return createPromise;
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
      return toast.promise(del(`${basePath}/skrivestotte/${id}`), {
        pending: "Sletter tekst...",
        success: "Tekst slettet",
        error: "Noe gikk galt under sletting av tekst",
      });
    },
    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: textsQueryOptions.queryKey,
      }),
  });
};
