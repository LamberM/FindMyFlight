# Documentation Conventions (MkDocs / TechDocs)

## Front-matter

Every `.md` under `docs/` **must** start with YAML front-matter:

```yaml
---
title: <Page Title>
description: <One-line description of the page>
tags:
  - <repo-name>
  - <topic1>
  - <topic2>
---
```

- `title` matches the `H1` heading that immediately follows the front-matter.
- `description` is one sentence, no trailing period.

## Formatting

- Use Markdown supported by the Backstage/TechDocs renderer; avoid extensions it does not support.
- Add a blank line **before** every ordered and unordered list — TechDocs requires it for correct rendering.
- Run `markdownlint` (IDE extension or CLI) before committing.
- Inline-code (backticks) for: file names, paths, class/method/field names, property keys, CLI commands, env vars — `config/cloud.properties`, `AbstractIdentifiable`, `LOG.info()`.

## Headings

- `# H1` — page title, immediately after front-matter.
- `## H2` — major sections.
- `### H3` — subsections.
- Never skip heading levels.

## Links

- Internal: relative path — `[text](../other-section/page.md)`.
- External: full URL — `[text](https://example.com)`.
- Never use absolute internal paths starting with `/`.
- Never use naked URLs — always provide link text.

## Admonitions

```markdown
!!! note
    Informational context.

!!! warning
    Potential data loss or irreversible action.

!!! tip
    Helpful shortcut or best practice.
```

## Code blocks

Always specify a language hint. Don't use line continuations if the line fits in 120 chars.

bash
    ./gradlew build
    java
    LOG.info("Cluster {} deployed", clusterId);
    

## Tables

Standard Markdown pipe tables, with blank lines before and after:

```markdown
| Column A | Column B |
|---|---|
| value    | value    |
```

## Navigation (`mkdocs.yaml`)

- The `nav:` section in `mkdocs.yaml` controls the sidebar — add new pages there.

## Images

- Place images in `docs/images/`.
- Reference relatively: `[alt text](../images/my-diagram.png)`.
- Prefer SVG or PNG. Avoid JPEG for diagrams.

## Diagrams

Mermaid (` ```mermaid `) for architecture and flow diagrams — see [`diagrams.md`](diagrams.md). Prefer Mermaid over static images.

## What not to do

- No raw HTML.
- No `<br>` for spacing — use blank lines.
- Do not duplicate content from another page — link to it instead.
