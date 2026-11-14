export type Roll = { sides: number; count: number; total: number }

export function parseRoll(result: string): Roll | null {
  const singleDie = /^Rolled a d(\d+): (\d+)$/.exec(result)
  if (singleDie) {
    return { sides: parseInt(singleDie[1],10), count: 1, total: parseInt(singleDie[2],10) }
  }


  const multipleDice = /^Rolled (\d+)d(\d+): \[(\d+(?:, \d+)*)\] \(total (\d+)\)$/.exec(result)
  if (multipleDice) {
    return { sides: parseInt(multipleDice[2],10), count: parseInt(multipleDice[1],10), total: parseInt(multipleDice[4],10) }
  }

  return null
}