import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { api } from './api'
import type { PlayerState, ToolCall } from './api'
import './App.css'
import { CharacterCard } from './CharacterCard'
import { DiceTray } from './DiceTray'
import { parseRoll } from './dice'
import type { Roll } from './dice'
import { ActivityPanel } from './ActivityPanel'



// One entry in the story: what the player typed and how the games master answered.
type Entry = { player: string; reply: string; toolCalls: ToolCall[] }

const GAME_KEY = 'agent-quest-game-id'

export default function App() {
  const [gameId, setGameId] = useState<string | null>(null)
  const [player, setPlayer] = useState<PlayerState | null>(null)
  const [story, setStory] = useState<Entry[]>([])
  const [text, setText] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [lastRoll, setLastRoll] = useState<{ roll: Roll; turn: number } | null>(null)
  const storyEnd = useRef<HTMLDivElement>(null)

  // On load: carry on with the saved game if the server still has it, otherwise start a new one.
  useEffect(() => {
    const saved = readSavedGame()
    const start = saved
      ? api.player(saved).then((p) => ({ gameId: saved, player: p })).catch(() => api.newGame())
      : api.newGame()
    start
      .then((game) => {
        setGameId(game.gameId)
        setPlayer(game.player)
        saveGame(game.gameId)
      })
      .catch(() => setError('Could not reach the game server. Is Spring Boot running on port 8080?'))
  }, [])

  // Braces matter: newer Chrome returns a Promise from scrollIntoView, and an effect may only return a clean-up function.
  useEffect(() => {
    storyEnd.current?.scrollIntoView({ behavior: 'smooth' })
  }, [story])

  async function send(event: FormEvent) {
    event.preventDefault()
    const action = text.trim()
    if (!gameId || !action || busy) return
    setBusy(true)
    setError(null)
    try {
      const result = await api.turn(gameId, action)
            const roll = result.toolCalls
        .filter((c) => c.name === 'roll_dice')
        .map((c) => parseRoll(c.result))
        .filter((r) => r !== null)
        .at(-1)
      setLastRoll(roll ? { roll, turn: story.length } : null)
      setStory((s) => [...s, { player: action, reply: result.reply, toolCalls: result.toolCalls }])
      setPlayer(result.player)
      setText('')
    } catch {
      setError('The games master did not answer. Try again.')
    } finally {
      setBusy(false)
    }
  }

  async function newGame() {
    const game = await api.newGame()
    setGameId(game.gameId)
    setPlayer(game.player)
    setStory([])
    saveGame(game.gameId)
  }
  const lastTurn = story.at(-1)
  return (
    <div className="layout">
      <header className="title">
        <h1>Agent Quest</h1>
        <button type="button" onClick={newGame} disabled={busy}>New game</button>
      </header>

      <main className="story" aria-live="polite">
        {story.length === 0 && <p className="hint">You arrive at a small village at dusk. What do you do?</p>}
        {story.map((entry, i) => (
          <article key={i} className="turn">
            <p className="you">{entry.player}</p>
            <p className="gm">{entry.reply}</p>
          </article>
        ))}
        {busy && <p className="hint">The games master is thinking…</p>}
        <div ref={storyEnd} />
      </main>

       <aside className="side">
        {player ? <CharacterCard player={player} /> : <p className="hint">Loading…</p>}
        {lastRoll && <DiceTray key={lastRoll.turn} roll={lastRoll.roll} />}
        {lastTurn && <ActivityPanel calls={lastTurn.toolCalls} />}
      </aside>

      <form className="input" onSubmit={send}>
        <label htmlFor="action" className="visually-hidden">Your action</label>
        <input id="action" value={text} onChange={(e) => setText(e.target.value)} maxLength={500}
               placeholder="I bet 5 gold that I can beat the blacksmith at arm wrestling" disabled={!gameId || busy} />
        <button type="submit" disabled={!gameId || busy || !text.trim()}>Act</button>
      </form>

      {error && <p className="error" role="alert">{error}</p>}
    </div>
  )
}

function readSavedGame(): string | null {
  try {
    return localStorage.getItem(GAME_KEY)
  } catch {
    return null
  }
}

function saveGame(id: string) {
  try {
    localStorage.setItem(GAME_KEY, id)
  } catch {
    // private browsing: the game still works, it just won't be resumed after a reload
  }
}
