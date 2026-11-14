import type { PlayerState } from './api'

const MAX_SPELL_SLOTS = 3

/** Bar colour: green above half, amber above a quarter, red otherwise. */
export function hpColour(hp: number, maxHp: number): 'green' | 'amber' | 'red' {
  const fraction = hp / maxHp
  if (fraction > 0.5) return 'green'
  if (fraction > 0.25) return 'amber'
  return 'red'
}

export function CharacterCard({ player }: { player: PlayerState }) {
  const hpPercent = (player.hp / player.maxHp) * 100

  return (
    <section className="card" aria-label="Character">
      <h2>Adventurer</h2>

      <div className="stat">
        <span>HP</span>
        <span>{`${player.hp} / ${player.maxHp}`}</span>
      </div>
      <div className="bar">
        <div className={`fill ${hpColour(player.hp, player.maxHp)}`} style={{ width: `${hpPercent}%` }} />
      </div>

      <div className="stat">
        <span>Gold</span>
        <span className="gold">{player.gold}</span>
      </div>

      <div className="stat">
        <span>Spells</span>
        <span className="pips">
          {Array.from({ length: MAX_SPELL_SLOTS }, (_, i) => (
            <span key={i} data-testid={i < player.spellSlots ? 'pip-full' : 'pip-empty'}
                  className={i < player.spellSlots ? 'pip full' : 'pip'} />
          ))}
        </span>
      </div>

      <h3>Inventory</h3>
      <ul className="inventory">
        {player.inventory.map((item) => <li key={item}>{item}</li>)}
      </ul>
    </section>
  )
}
