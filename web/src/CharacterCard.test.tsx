import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { CharacterCard, hpColour } from './CharacterCard'
import type { PlayerState } from './api'

const hero: PlayerState = {
  hp: 15, maxHp: 20, strength: 3, spellSlots: 2, gold: 12,
  inventory: ['rusty sword', 'leather armor'],
}

describe('CharacterCard', () => {
  it('shows HP as current out of max', () => {
    render(<CharacterCard player={hero} />)
    expect(screen.getByText('15 / 20')).toBeTruthy()
  })

  it('shows gold and every inventory item', () => {
    render(<CharacterCard player={hero} />)
    expect(screen.getByText('12')).toBeTruthy()
    expect(screen.getByText('rusty sword')).toBeTruthy()
    expect(screen.getByText('leather armor')).toBeTruthy()
  })

  it('lights one pip per spell slot left, out of 3', () => {
    render(<CharacterCard player={hero} />)
    expect(screen.getAllByTestId('pip-full')).toHaveLength(2)
    expect(screen.getAllByTestId('pip-empty')).toHaveLength(1)
  })
})

describe('hpColour', () => {
  it('is green above half, amber above a quarter, red otherwise', () => {
    expect(hpColour(20, 20)).toBe('green')
    expect(hpColour(10, 20)).toBe('amber')   // exactly half is not "above half"
    expect(hpColour(5, 20)).toBe('red')      // exactly a quarter is not "above a quarter"
  })
})