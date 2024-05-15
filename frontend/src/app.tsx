import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider, createRouter } from "@tanstack/react-router";
import { QueryClientProvider } from "@tanstack/react-query";
import "@navikt/ds-css";
import "./index.css";

const startMsw = async () => {
  if (import.meta.env.VITE_MOCK === "true") {
    const { worker } = await import("./mocks/browser");
    return worker.start();
  }
};

// Import the generated route tree
import { routeTree } from "./routeTree.gen";
import { queryClient } from "./queryClient";
import { Loader } from "@navikt/ds-react";

// Create a new router instance
const router = createRouter({ routeTree });

// Register the router instance for type safety
declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}

await startMsw();

// Render the app
const rootElement = document.getElementById("app")!;
if (!rootElement.innerHTML) {
  const root = ReactDOM.createRoot(rootElement);
  root.render(
    <React.StrictMode>
      <QueryClientProvider client={queryClient}>
        <React.Suspense fallback={<Loader />}>
          <RouterProvider router={router} />
        </React.Suspense>
      </QueryClientProvider>
    </React.StrictMode>,
  );
}
