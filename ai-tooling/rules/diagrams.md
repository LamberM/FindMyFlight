# Diagram Conventions (Mermaid in TechDocs)

## Tool

Use **Mermaid** for all architecture, flow, and sequence diagrams. Mermaid is rendered **client-side**
by the  addon — no `mkdocs.yaml` plugin changes are needed.

Reference: <>

## Embedding

- Never embed diagram source **inline** in the `.md` file. Always create a separate `.mmd` file and include it via `pymdownx.snippets` (`--8<--`) inside a mermaid fenced code block.
- Place the `.mmd` file in the documentation section it belongs to (e.g. `docs/infrastructure/`) — not in a separate `diagrams/` folder.
- Name the `.mmd` file with a descriptive kebab-case slug matching the subject.

## Accessibility

- Always include a **text description** (paragraph or bullet list) before or after the Mermaid block describing what the diagram shows. Screen readers and text search cannot parse rendered diagrams.
- Legend tables and text descriptions belong in the `.md` file, not in the `.mmd` source.

## Styling

Always start every `.mmd` file with a white-background init directive so it renders consistently across TechDocs, local preview, and CLI export:

```text
%%{init: {'theme': 'base', 'themeVariables': {'background': '#ffffff'}}}%%
```

Use `classDef` for consistent, light brand colors:

```text
classDef aws fill: #FFF3E0,stroke: #E65100,color: #333      %% AWS services
classDef app fill: #EDE7F6,stroke: #512DA8,color: #333      %% Application components
classDef data fill: #E8F5E9,stroke: #2E7D32,color: #333     %% Data stores
classDef external fill: #F5F5F5,stroke: #616161,color: #333 %% External actors / clients
classDef lb fill: #E3F2FD,stroke: #1565C0,color: #333       %% Load balancers
```

`classDef` and `class` style directives belong in the `.mmd` file. Add a **Legend** table below the diagram in the `.md` file mapping colors to meanings.

## Complexity

- Keep diagrams under ~40 nodes. If larger, split into multiple focused diagrams (network topology, data flow, service interactions) on the same page or across pages.
- Use `subgraph` blocks to group related nodes and reduce visual clutter.

## Verification

Render `.mmd` files locally before committing:

```bash
npx -y @mermaid-js/mermaid-cli@latest -i <file>.mmd -o <file>.png --scale 3 --width 2048 --backgroundColor white
```

The `.png` is for validation only — do not commit it. For interactive prototyping, use the **Mermaid Live Editor** at <https://mermaid.ai/live/edit>.

## What not to do

- Do not embed raw `<svg>` or `<img>` tags — use standard Markdown image syntax for fallback images.
