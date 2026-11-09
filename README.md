# Agent Quest (Java)

An AI agent written in plain Java: a tabletop RPG games master that rolls dice, looks up rules and tracks the player's character, powered by a model on **Amazon Bedrock**.

It follows the idea of AWS's "Build your first AI agent" tutorial (Mike Chambers, which uses the Python Strands Agents SDK), but in Java and **without an agent framework**: I write the agentic loop myself on the AWS SDK for Java's Converse API, so every step the framework would hide is visible.

**Status:** in progress (Step 2 of 5).

## What makes it an agent

- **A model** (the brain): Amazon Nova 2 Lite on Bedrock. It reads the conversation and decides whether to answer or to use a tool.
- **Tools** (the hands): plain Java methods. The model can ask for them; only my code runs them.
- **A system prompt** (the personality and rules).
- **The agentic loop**: send the conversation; if the model asks for a tool, run it, send back the result, and ask again; stop when it answers. With a limit on rounds, so it can't loop forever.

## Plan

| Step | What | Status |
|---|---|---|
| 1 | The tools in plain Java, with JUnit tests: `Dice`, `RuleBook`, `Player` | in progress |
| 2 | One call to Nova 2 Lite through the Converse API (no tools) | |
| 3 | Tool definitions (JSON schema) and the agentic loop | |
| 4 | Chat loop: play the game; token usage per session | |
| 5 | Swap the model (Claude Haiku 4.5) with one line; compare | |

## Build log

### Step 1: the tools (done)
Three plain Java classes the model will be able to call, with 13 JUnit tests written first:
- `Dice`: rolls with a `Random` passed in, so tests can use a fixed seed and get repeatable rolls.
- `RuleBook`: splits a text file into sections and returns the one that mentions a topic; a miss lists the sections so the model can try again. Loaded from `src/main/resources` with try-with-resources.
- `Player`: HP, gold, spell slots and inventory. The state lives in Java, not in the model, because a model can't be trusted to remember numbers.

What I got wrong first: my gold rule subtracted before checking, so a failed purchase wiped the player's gold. Fixed by validating before changing anything: refuse the transaction rather than half-do it.

## Run it

Java 21 and Maven. `mvn test` runs the tool tests, no AWS needed. From Step 2, Bedrock calls use my `ask-barry` AWS profile, which assumes a least-privilege role allowed to call only two models in the EU (Nova 2 Lite and Claude Haiku 4.5). No keys are in the code.
