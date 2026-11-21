export const STARTERS = [
  'Look around the village',
  'Head to the tavern for news',
  'Visit the market to spend my gold',
  'Follow the road out of town',
]

type Props = {
  onPick: (action: string) => void   // what to do when a chip is clicked (App passes its play function)
  disabled: boolean
}

export function WelcomeCard({ onPick, disabled }: Props) {
  return (
    <section className="welcome">
      <h2>Welcome, adventurer</h2>
      <p>You are an adventurer. Type what you do, or pick a start below.</p>
      <div className="chips">
        {STARTERS.map((s) => (
          <button key={s} type="button" className="chip" disabled={disabled} onClick={() => onPick(s)}>
            {s}
          </button>
        ))}
      </div>
    </section>
  )
}