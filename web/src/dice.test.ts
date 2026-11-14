import { describe, expect, it } from 'vitest'
import { parseRoll } from './dice'

describe('parseRoll', () => {
  it('reads a single die', () => {
    expect(parseRoll('Rolled a d20: 14')).toEqual({ sides: 20, count: 1, total: 14 })
  })

  it('reads several dice and uses the total', () => {
    expect(parseRoll('Rolled 3d6: [2, 5, 1] (total 8)')).toEqual({ sides: 6, count: 3, total: 8 })
  })

  it('returns null for anything that is not a roll', () => {
    expect(parseRoll('Gold is now 15')).toBeNull()
  })
})