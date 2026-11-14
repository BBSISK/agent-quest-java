import type { ToolCall } from './api'

export function ActivityPanel({ calls }: { calls: ToolCall[] }) {
  return (
    <section className="activity" aria-label="Agent activity">
      <h2>Agent activity</h2>
      {calls.length === 0 ? (
        <p className="no-tools">No tools used this turn</p>
      ) : (
        <ol>
          {calls.map((c, i) => (
            <li key={i} className={`call ${c.name}`}>
              <span className="tool">{c.name}</span>
              <span className="input">{c.input}</span>
                <div className="result">{c.result}</div>
              </li>
          ))}
        </ol>
      )}
    </section>
  )
}