# Code comments & self-documenting code

Loads on any code-writing task.

Default to **fewer comments**. Prefer self-documenting code and the commit message over prose in the source.

1. **Self-document first.** Clear names, small functions, and obvious structure should carry the meaning — reach for those before a comment.
2. **Put the *why* in the commit message, not the code.** Rationale, trade-offs, ticket references, and background belong in the commit body, where reviewers and `git blame` look — not inline.
3. **Comment only the genuinely non-obvious** — an external-behaviour workaround, a subtle invariant, a constraint the code cannot express. If the code can be made clear instead, do that.
4. **When you do comment, keep it one tight line** — state the fact, don't re-derive the context. A comment growing into half the context is a sign it belongs in the commit message.

Do not narrate what the code already says, and do not paste multi-line background into source files.
