import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/tekster/")({
  component: Index,
});

function Index() {
  return <div>Ingen tekster valgt</div>;
}
