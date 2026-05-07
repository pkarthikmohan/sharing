# Cyber-Security-Prototype
#Aegis

Monorepo prototype for a small cyber-security themed system: an **Express API server**, a **Postgres + Drizzle** data layer, and an **OpenAPI-driven** client/tooling setup (Zod schemas + React Query hooks), plus a **Vite UI mockup sandbox**.

> **Repo layout note**: the actual pnpm workspace lives in the `Cyber-Security-Prototype/` subfolder.

## What’s inside

- **API server** (`Cyber-Security-Prototype/artifacts/api-server`)
  - Express 5 app mounted under `/api`
  - Health endpoint: `GET /api/healthz`
  - Structured logging via `pino` / `pino-http` (with redaction for auth/cookies)
- **Database package** (`Cyber-Security-Prototype/lib/db`)
  - Postgres connection pool via `pg`
  - Drizzle ORM with schema exported from `src/schema`
  - Drizzle Kit commands for pushing schema in dev
- **OpenAPI spec** (`Cyber-Security-Prototype/lib/api-spec/openapi.yaml`)
  - Source-of-truth contract for the API
- **Generated types/clients**
  - **Zod schemas** (`Cyber-Security-Prototype/lib/api-zod`) generated from OpenAPI via Orval
  - **React Query client** (`Cyber-Security-Prototype/lib/api-client-react`) generated from OpenAPI via Orval
- **UI mockup sandbox** (`Cyber-Security-Prototype/artifacts/mockup-sandbox`)
  - Vite + React + Tailwind sandbox for prototyping UI (infinite canvas-style mockups)

## Prerequisites

- **Node.js**: the workspace is set up for modern Node (the included docs mention Node 24).
- **pnpm**: required (the repo blocks npm/yarn installs).
- **PostgreSQL**: required for anything that touches `@workspace/db`.

## Quickstart (Windows / PowerShell)

From the repo root:

```powershell
cd .\Cyber-Security-Prototype\
pnpm install
```

### 1) Run the API server

The server requires both `DATABASE_URL` and `PORT`.

```powershell
$env:DATABASE_URL="postgres://USER:PASSWORD@localhost:5432/DBNAME"
$env:PORT="5000"
pnpm --filter @workspace/api-server run dev
```

Verify:

- `GET http://localhost:5000/api/healthz` → `{"status":"ok"}`

### 2) (Dev) Push the DB schema

This uses Drizzle Kit and requires `DATABASE_URL`.

```powershell
$env:DATABASE_URL="postgres://USER:PASSWORD@localhost:5432/DBNAME"
pnpm --filter @workspace/db run push
```

### 3) Run the UI mockup sandbox

The sandbox requires `PORT` and `BASE_PATH`.

```powershell
$env:PORT="5173"
$env:BASE_PATH="/"
pnpm --filter @workspace/mockup-sandbox run dev
```

## Environment variables

- **API server** (`artifacts/api-server`)
  - **`PORT`**: required (server will throw if missing/invalid)
  - **`DATABASE_URL`**: required by `@workspace/db` (and any API routes that import it)
  - **`LOG_LEVEL`**: optional (defaults to `info`)
- **Mockup sandbox** (`artifacts/mockup-sandbox`)
  - **`PORT`**: required
  - **`BASE_PATH`**: required (Vite `base` setting; use `/` for local dev)

## OpenAPI + codegen workflow

The OpenAPI spec is the contract and drives generated code for:

- `@workspace/api-zod` (Zod request/response schemas + TS types)
- `@workspace/api-client-react` (React Query hooks/client)

To regenerate from `lib/api-spec/openapi.yaml`:

```powershell
cd .\Cyber-Security-Prototype\
pnpm --filter @workspace/api-spec run codegen
```

## Useful workspace commands

Run these from `Cyber-Security-Prototype/`:

```powershell
pnpm run typecheck
pnpm run build
```

## Repo map

```text
.
├─ .github/                          # GitHub config
└─ Cyber-Security-Prototype/         # pnpm workspace root
   ├─ artifacts/
   │  ├─ api-server/                 # Express API (build via esbuild)
   │  └─ mockup-sandbox/             # Vite + React UI prototyping sandbox
   ├─ lib/
   │  ├─ api-spec/                   # OpenAPI spec + Orval config
   │  ├─ api-zod/                    # Generated Zod schemas/types
   │  ├─ api-client-react/           # Generated React Query client
   │  └─ db/                         # Postgres + Drizzle ORM + schema
   └─ scripts/                       # Misc scripts
```

## Security notes (prototype-friendly defaults)

- **Supply-chain defense**: `pnpm-workspace.yaml` enforces a **minimum release age** for npm packages (with allowlist exceptions).
- **Log redaction**: API logging redacts common secret-bearing headers (`authorization`, `cookie`, `set-cookie`).

## Troubleshooting

- **“Use pnpm instead” during install**: run `pnpm install` (this repo intentionally blocks npm/yarn installs).
- **API server crashes on startup**: ensure **both** `PORT` and `DATABASE_URL` are set.
- **Mockup sandbox crashes on startup**: ensure **both** `PORT` and `BASE_PATH` are set (try `BASE_PATH=/`).

