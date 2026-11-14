import { useEffect, useState } from 'react'
import type { Roll } from './dice'

const ROLL_TIME_MS = 900

export function DiceTray({ roll }: { roll: Roll }) {
  const [face, setFace] = useState(roll.total)
  const [rolling, setRolling] = useState(true)

  useEffect(() => {
    // While "rolling", flash random faces in the possible range, then land on the real total.
    const lowest = roll.count
    const range = roll.count * (roll.sides - 1) + 1
    const flicker = setInterval(() => setFace(lowest + Math.floor(Math.random() * range)), 70)
    const land = setTimeout(() => {
      clearInterval(flicker)
      setFace(roll.total)
      setRolling(false)
    }, ROLL_TIME_MS)
    // Clean-up: if the component goes away mid-roll, stop the timers.
    return () => {
      clearInterval(flicker)
      clearTimeout(land)
    }
  }, [roll])

  const natural = roll.count === 1 && !rolling
  const extra = natural && roll.total === roll.sides ? 'crit' : natural && roll.total === 1 ? 'fumble' : ''
  const name = `${roll.count > 1 ? roll.count : ''}d${roll.sides}`

  return (
    <section className="tray" aria-label="Dice">
      <h2>Dice</h2>
      <div className={`die ${rolling ? 'rolling' : 'landed'} ${extra}`}>
        <span>{face}</span>
      </div>
      <p className="die-label" aria-live="polite">{rolling ? `Rolling ${name}…` : `${name}: ${roll.total}`}</p>
    </section>
  )
}