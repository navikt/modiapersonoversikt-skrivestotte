import {
  Box,
  Button,
  HStack,
  Heading,
  Search,
  Spacer,
  Tag,
  VStack,
} from "@navikt/ds-react";
import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { queryClient } from "src/queryClient";
import { textsQueryOptions } from "src/queries";
import { useSuspenseQuery } from "@tanstack/react-query";
import { tagsQuerySearch } from "src/utils";
import { Tekst, UUID } from "src/model";
import { Outlet, useParams, useNavigate } from "@tanstack/react-router";

export const Route = createFileRoute("/tekster")({
  component: Texts,
  loader: () => queryClient.ensureQueryData(textsQueryOptions),
});

const matcher = tagsQuerySearch<Tekst>(
  (tekst) => tekst.tags,
  (tekst) => [
    tekst.id || "",
    tekst.overskrift,
    Object.values(tekst.innhold).join("\u0000"),
    tekst.tags.join("\u0000"),
  ],
);
const searchTexts = (
  tekster: Array<Tekst>,
  query: string,
  valgtTekst: UUID,
): Array<Tekst> => matcher(query, tekster, (tekst) => tekst.id === valgtTekst);

function Texts() {
  const [search, setSearch] = useState("");
  const { textId } = useParams({ strict: false }) as { textId?: string };
  const navigate = useNavigate({ from: Route.path });
  const texts = useSuspenseQuery(textsQueryOptions);

  const filteredTexsts = searchTexts(
    Object.values(texts.data),
    search,
    textId ?? "",
  );

  const selectedText = textId;
  const onSelect = (textId: string) =>
    navigate({ to: "/tekster/$textId", params: { textId } });

  return (
    <Box>
      <HStack justify="space-between">
        <Box>
          <form
            role="search"
            onSubmit={(e) => {
              e.preventDefault();
            }}
          >
            <Search
              label="Søk i tekster"
              variant="simple"
              onInput={(e) => setSearch(e.currentTarget.value)}
              onClear={() => setSearch("")}
            />
          </form>
        </Box>
        <Spacer />
        <Box>
          <Button
            variant="secondary"
            onClick={() => navigate({ to: "/tekster/ny", from: "/tekster" })}
          >
            Legg til ny tekst
          </Button>
        </Box>
      </HStack>

      <HStack style={{ marginTop: "1em" }} gap="4">
        <Box
          style={{ height: "80vh", overflow: "scroll", width: "16em" }}
          borderWidth="1"
        >
          <VStack>
            {filteredTexsts.map((text) => {
              const isSelected = selectedText === text.id;
              return (
                <Box
                  className="hover-selected"
                  onClick={() => onSelect(text.id)}
                  padding="2"
                  borderWidth={isSelected ? "2" : "1"}
                  borderColor={isSelected ? "border-selected" : undefined}
                  background={isSelected ? "surface-selected" : undefined}
                >
                  <Heading size="xsmall">{text.overskrift}</Heading>
                  <HStack gap="2">
                    {text.tags.map((t) => (
                      <Tag size="small" variant="info">
                        {t}
                      </Tag>
                    ))}
                  </HStack>
                </Box>
              );
            })}
          </VStack>
        </Box>
        <Outlet />
      </HStack>
    </Box>
  );
}
