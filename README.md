# Agent Quest (Java)

An AI agent written in plain Java: a tabletop RPG games master that rolls dice, looks up rules and tracks the player's character, powered by a model on **Amazon Bedrock**.

It follows the idea of AWS's "Build your first AI agent" tutorial (Mike Chambers, which uses the Python Strands Agents SDK), but in Java and **without an agent framework**: I wrote the agentic loop myself on the AWS SDK for Java's Converse API, so every step a framework would hide is visible, and tested.

**Status:** complete (5 of 5 steps), November 2026. 24 JUnit tests; a benchmark that scores two models on the same scripted game.

## Result

The same 4-turn game (buy an ale, bet on arm wrestling, cast a fireball, check the character), 3 games per model, scored **only from the tool log**: what the agent actually ran, not what it said.

| Model (Bedrock, eu-west-1) | Prompt | State changes recorded | All required tool calls | Avg per turn | Tokens per game (in / out) |
|---|---|---|---|---|---|
| Amazon Nova 2 Lite | baseline | **0 / 9** | 10 / 21 | 2.5 s | 18,869 / 768 |
| Claude Haiku 4.5 | baseline | **9 / 9** | 20 / 21 | 3.3 s | 24,551 / 1,268 |
| Amazon Nova 2 Lite | + "never say it changed unless you called the tool" | **0 / 9** | 7 / 21 | 2.0 s | 12,459 / 706 |
| Claude Haiku 4.5 | + the same rule | **9 / 9** | 21 / 21 | 3.3 s | 24,295 / 1,310 |

What I take from it:
- **Choosing the model mattered far more than the prompt.** Nova narrated gold, damage and spell slots changing but never once recorded a change in 18 chances, even when told explicitly. Haiku recorded every one, for about a third more tokens and under a second more per turn. The game now runs on Haiku: a one-line change, because the loop doesn't care which model is behind `ModelClient`.
- **An agent can say it did something without doing it.** The narration looked right; only the tool log showed the game state had not changed. Judge an agent on what it does.
- **Small sample:** one run of 3 games per row. The 0/9 vs 9/9 gap is clear; the smaller differences are noise.
- **A real model found a bug the tests didn't.** Haiku sometimes returns an empty reply. My loop added it to history, and Bedrock refused the next request (`content field ... is empty`, HTTP 400). Fixed with a placeholder message, and a test now covers it.

## What makes it an agent

- **A model** (the brain): Claude Haiku 4.5 (or Nova 2 Lite) on Bedrock. It reads the conversation and decides whether to answer or to use a tool.
- **Tools** (the hands): plain Java methods. The model can only ask for them; my code runs them.
- **A system prompt** (the personality and rules).
- **The agentic loop**: send the conversation; if the model asks for a tool, run it, send back the result, and ask again; stop when it answers, or after 8 rounds so it can't loop (and spend) forever.

## Plan

| Step | What | Status |
|---|---|---|
| 1 | The tools in plain Java, with JUnit tests: `Dice`, `RuleBook`, `Player` | done |
| 2 | One call to Nova 2 Lite through the Converse API (no tools) | done |
| 3 | Tool definitions (JSON schema) and the agentic loop | done |
| 4 | Chat loop: play the game; token usage per session | done |
| 5 | Benchmark Nova vs Claude Haiku 4.5, with and without a prompt rule | done |

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

### Step 3: tools and the agentic loop (done)
- `ToolBox` describes the 4 tools to the model as JSON schemas, and `run(name, input)` dispatches a request to my Java code. An unknown tool name returns a message rather than throwing, because a model can invent names.
- `Agent.ask` is the loop: send the conversation; if the stop reason is `tool_use`, run every requested tool, send the results back in one message, matched by `toolUseId`, and ask again; stop at the first real answer, or after 8 rounds.
- `ModelClient` is a one-method interface, so 10 tests run the loop against a scripted fake model: no AWS, no cost, the same result every time. They check the toolUseId link, that history carries over, and the round limit.
- My bug: I first sent tool results back as plain text. That loses the toolUseId link, so Bedrock would see the player talking rather than the tool answering. The test caught it before it reached AWS.

### Step 4: playing the game (done)
A chat loop over the agent, printing every tool call under each answer, and a token counter written as a lambda around the real model call (the `ModelClient` interface made that a two-line change).

What the tool log caught: in an arm-wrestling bet, Nova 2 Lite rolled the dice correctly, then told the player "Gold: 15" without ever calling `update_player_state`, and it invented the opponent's strength bonus instead of looking up the rule. The narration and the real game state drifted apart. Lesson: an agent can say it did something without doing it; trust the tool log, not the text. Step 5 tests whether a stricter prompt or a different model fixes it.

### Step 5: measuring two models (done)
`Benchmark` plays a fixed 4-turn script on each model and checks, turn by turn, whether the tools the story needs were called (for example, a bet must look up the rule, roll the dice and update the gold). A `baseline` option removes the new prompt rule, so the same code measures before and after. Results above.
- Bug found by Haiku: an empty assistant reply went into history and Bedrock rejected the next request. Fixed in `Agent.ask` (a placeholder message keeps the user/assistant turns alternating); test `emptyReplyIsNotSentBack` written first, failing, then passing.
- `mvn exec:java -Dexec.mainClass=...` was ignored because the pom hard-coded the main class; it's now a property with GameMaster as the default.

## Run it

Java 21 and Maven. `mvn test` runs all 24 tests with no AWS needed (the loop is tested against a scripted fake model).

```bash
mvn -q compile exec:java                                                         # play
mvn -q compile exec:java -Dexec.mainClass=quest.Benchmark -Dexec.args="3"        # benchmark
mvn -q compile exec:java -Dexec.mainClass=quest.Benchmark -Dexec.args="3 baseline"
```

Bedrock calls use my `ask-barry` AWS profile, which assumes a least-privilege role allowed to call only two models in the EU (Nova 2 Lite and Claude Haiku 4.5). No keys are in the code.
