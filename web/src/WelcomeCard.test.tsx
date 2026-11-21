import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { STARTERS, WelcomeCard } from './WelcomeCard'

describe('WelcomeCard', () => {
  it('offers the starter actions and sends the one you click', () => {
    const onPick = vi.fn()
    render(<WelcomeCard onPick={onPick} disabled={false} />)
    expect(screen.getAllByRole('button')).toHaveLength(STARTERS.length)
    fireEvent.click(screen.getByText(STARTERS[1]))
    expect(onPick).toHaveBeenCalledWith(STARTERS[1])
  })
}

)