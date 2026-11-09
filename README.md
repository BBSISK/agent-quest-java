# Agent Quest (Java)

An AI agent written in plain Java: a tabletop RPG games master that rolls dice, looks up rules and tracks the player's character, powered by a model on **Amazon Bedrock**.

It follows the idea of AWS's "Build your first AI agent" tutorial (Mike Chambers, which uses the Python Strands Agents SDK), but in Java and **without an agent framework**: I write the agentic loop myself on the AWS SDK for Java's Converse API, so every step the framework would hide is visible.

**Status:** in progress (Step 3 of 5).

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


### Step 2: first call to Bedrock (done)
One request to Amazon Nova 2 Lite through the Converse API: a system prompt, one user message, the reply and its stop reason (`end_turn`) and token counts (82 in, 249 out).
- Credentials come from my `ask-barry` AWS profile, which assumes a role allowed to call only two EU models. Nothing secret is in the code.
- A new laptop had no AWS profile at all (`ProfileFile(sections=[])`): credentials belong to the machine, not the repo. I gave the new Mac its own access key and rebuilt the role profile.
- First bug: `ClassNotFoundException`, because I'd saved the class under `src/main/quest` instead of `src/main/java/quest`. Maven only compiles code under `src/main/java`.
## Run it


### Step 3: tools and the agentic loop (done)
- `ToolBox` describes the 4 tools to the model as JSON schemas, and `run(name, input)` dispatches a request to my Java code. An unknown tool name returns a message rather than throwing, because a model can invent names.
- `Agent.ask` is the loop: send the conversation; if the stop reason is `tool_use`, run every requested tool, send the results back in one message, matched by `toolUseId`, and ask again; stop at the first real answer, or after 8 rounds.
- `ModelClient` is a one-method interface, so 10 tests run the loop against a scripted fake model: no AWS, no cost, the same result every time. They check the toolUseId link, that history carries over, and the round limit.
- My bug: I first sent tool results back as plain text. That loses the toolUseId link, so Bedrock would see the player talking rather than the tool answering. The test caught it before it reached AWS.

### Step 4: playing the game (done)
A chat loop over the agent, printing every tool call under each answer, and a token counter written as a lambda around the real model call (the `ModelClient` interface made that a two-line change).

What the tool log caught: in an arm-wrestling bet, Nova 2 Lite rolled the dice correctly, then told the player "Gold: 15" without ever calling `update_player_state`, and it invented the opponent's strength bonus instead of looking up the rule. The narration and the real game state drifted apart. Lesson: an agent can say it did something without doing it; trust the tool log, not the text. Step 5 tests whether a stricter prompt or a different model fixes it.

Java 21 and Maven. `mvn test` runs the tool tests, no AWS needed. From Step 2, Bedrock calls use my `ask-barry` AWS profile, which assumes a least-privilege role allowed to call only two models in the EU (Nova 2 Lite and Claude Haiku 4.5). No keys are in the code.
