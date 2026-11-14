import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ActivityPanel } from './ActivityPanel'

describe('ActivityPanel', () => {
  it('lists every tool call with its result, in order', () => {
    render(<ActivityPanel calls={[
      { name: 'roll_dice', input: '{"sides": 20}', result: 'Rolled a d20: 14' },
      { name: 'update_player_state', input: '{"goldChange": -5}', result: 'Gold is now 5' },
    ]} />)
    const items = screen.getAllByRole('listitem')
    expect(items).toHaveLength(2)
    expect(items[0].textContent).toContain('roll_dice')
    expect(items[1].textContent).toContain('Gold is now 5')
  })

  it('says so when the games master used no tools', () => {
    render(<ActivityPanel calls={[]} />)
    expect(screen.getByText('No tools used this turn')).toBeTruthy()
  })
})