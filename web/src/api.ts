// Typed client for the Agent Quest REST API (the Spring Boot GameController).
// The types mirror the Java records: PlayerState, Agent.ToolCall, GameService.TurnResult, GameController.NewGame.

export type PlayerState = {
  hp: number
  maxHp: number
  strength: number
  spellSlots: number
  gold: number
  inventory: string[]
}

export type ToolCall = {
  name: string // "roll_dice" | "lookup_rule" | "get_player_state" | "update_player_state"
  input: string // the tool's arguments as JSON text, e.g. {"sides": 20}
  result: string // what the Java tool answered, e.g. "Rolled a d20: 16"
}

export type TurnResult = {
  reply: string
  toolCalls: ToolCall[]
  player: PlayerState
}

export type NewGame = {
  gameId: string
  player: PlayerState
}

async function call<T>(method: 'GET' | 'POST', path: string, body?: unknown): Promise<T> {
  const response = await fetch(path, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  if (!response.ok) {
    throw new Error(`${method} ${path} failed: ${response.status}`)
  }
  return (await response.json()) as T
}

export const api = {
  newGame: () => call<NewGame>('POST', '/api/games'),
  player: (gameId: string) => call<PlayerState>('GET', `/api/games/${gameId}`),
  turn: (gameId: string, text: string) => call<TurnResult>('POST', `/api/games/${gameId}/turns`, { text }),
}
