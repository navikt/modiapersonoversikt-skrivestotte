import { Box } from "@navikt/ds-react";
import { createFileRoute } from "@tanstack/react-router";
import TextEditor from "src/components/TextEditor";

export const Route = createFileRoute("/tekster/ny")({
  component: EditText,
});

function EditText() {
  return (
    <Box className="grow">
      <TextEditor isNew />
    </Box>
  );
}
