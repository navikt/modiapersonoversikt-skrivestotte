import { Box, HStack, InternalHeader, Link, Page } from "@navikt/ds-react";
import {
  createRootRoute,
  Link as RouterLink,
  Outlet,
} from "@tanstack/react-router";
import { TanStackRouterDevtools } from "@tanstack/router-devtools";

export const Route = createRootRoute({
  component: () => (
    <>
      <Page>
        <InternalHeader style={{ position: "sticky" }}>
          <InternalHeader.Title>
            Modiapersonoversikt - Skrivestøtte admin
          </InternalHeader.Title>
          <Box padding="4">
            <HStack gap="4">
              <Link as={RouterLink} to="/">
                Hjem
              </Link>
              <Link as={RouterLink}>Statistikk</Link>
            </HStack>
          </Box>
        </InternalHeader>
        <Box as="main" padding="8">
          <Page.Block>
            <Outlet />
          </Page.Block>
        </Box>
      </Page>
      <TanStackRouterDevtools />
    </>
  ),
});
