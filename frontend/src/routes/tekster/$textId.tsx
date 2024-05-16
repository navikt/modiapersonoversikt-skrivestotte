import { Alert, Box } from "@navikt/ds-react";
import { createFileRoute } from "@tanstack/react-router";
import TextEditor from "src/components/TextEditor";
import { useText } from "src/queries";

export const Route = createFileRoute("/tekster/$textId")({
  component: EditText,
});

function EditText() {
  const { textId } = Route.useParams();
  const text = useText(textId);

  return (
    <Box className="grow">
      {text ? (
        <TextEditor text={text} />
      ) : (
        <Alert variant="warning">Fant ikke teksten</Alert>
      )}
    </Box>
  );
}
