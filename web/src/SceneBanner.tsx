// F2: a hand-drawn SVG scene for each kind of place (Journey.KINDS in Java).
// Flat shapes only, so it scales to any width and costs nothing to load.

type Props = { kind: string; place: string }

const SKY: Record<string, [string, string]> = {
  village: ['#3b2a4a', '#c9774a'],
  tavern: ['#2a1a10', '#4a2e1a'],
  market: ['#4a6a8a', '#e3c27b'],
  forest: ['#0f1a14', '#24402e'],
  road: ['#4a5a7a', '#d9a86a'],
  cave: ['#08070a', '#1c1712'],
  castle: ['#1f2440', '#6a5a8a'],
  temple: ['#2a2440', '#b98a5a'],
}

export function SceneBanner({ kind, place }: Props) {
  const [top, bottom] = SKY[kind] ?? SKY.road
  return (
    <figure className="scene" key={kind}>
      <svg viewBox="0 0 600 160" preserveAspectRatio="xMidYMid slice" role="img" aria-label={`${place} (${kind})`}>
        <defs>
          <linearGradient id={`sky-${kind}`} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0" stopColor={top} />
            <stop offset="1" stopColor={bottom} />
          </linearGradient>
        </defs>
        <rect width="600" height="160" fill={`url(#sky-${kind})`} />
        {scene(kind)}
      </svg>
      <figcaption>{place}</figcaption>
    </figure>
  )
}

function scene(kind: string) {
  switch (kind) {
    case 'village':
      return (
        <g>
          <circle cx="500" cy="40" r="16" fill="#f3d9a0" opacity="0.9" />
          <rect y="128" width="600" height="32" fill="#2a2018" />
          {[40, 150, 270, 390].map((x, i) => (
            <g key={x} transform={`translate(${x} ${i % 2 ? 70 : 78})`}>
              <rect width="80" height="56" y="0" fill="#3a2a20" />
              <polygon points="-8,2 40,-30 88,2" fill="#5a2e22" />
              <rect x="14" y="16" width="14" height="14" fill="#e3a857" />
              <rect x="50" y="16" width="14" height="14" fill={i % 2 ? '#e3a857' : '#2a1d16'} />
              <rect x="32" y="34" width="16" height="22" fill="#1b130d" />
            </g>
          ))}
        </g>
      )
    case 'tavern':
      return (
        <g>
          {[0, 1, 2, 3, 4, 5].map((i) => <rect key={i} x={i * 100} width="4" height="110" fill="#22150c" />)}
          <rect y="105" width="600" height="18" fill="#6a4426" />
          <rect y="123" width="600" height="37" fill="#3a2414" />
          {[120, 180, 380, 450].map((x) => (
            <g key={x} transform={`translate(${x} 82)`}>
              <rect width="18" height="24" rx="3" fill="#c9a26a" />
              <rect x="18" y="5" width="6" height="12" rx="3" fill="none" stroke="#c9a26a" strokeWidth="3" />
              <rect width="18" height="6" fill="#f3ead2" />
            </g>
          ))}
          <circle cx="300" cy="34" r="40" fill="#e3a857" opacity="0.18" />
          <rect x="292" y="20" width="16" height="22" rx="3" fill="#e3a857" />
          <line x1="300" y1="0" x2="300" y2="20" stroke="#22150c" strokeWidth="3" />
        </g>
      )
    case 'market':
      return (
        <g>
          <rect y="130" width="600" height="30" fill="#8a6a4a" />
          {[30, 175, 320, 465].map((x, i) => (
            <g key={x} transform={`translate(${x} 60)`}>
              {[0, 1, 2, 3, 4, 5].map((s) => (
                <rect key={s} x={s * 18} width="18" height="22" fill={s % 2 ? '#f3ead2' : ['#b33a2a', '#3f6a9a', '#3f9a46', '#b9772e'][i]} />
              ))}
              <rect x="6" y="22" width="4" height="48" fill="#4a3020" />
              <rect x="98" y="22" width="4" height="48" fill="#4a3020" />
              <rect x="0" y="50" width="108" height="20" fill="#6a4426" />
              <circle cx="30" cy="46" r="6" fill="#e0604f" />
              <circle cx="46" cy="46" r="6" fill="#e3a857" />
              <circle cx="62" cy="46" r="6" fill="#6fbf73" />
            </g>
          ))}
        </g>
      )
    case 'forest':
      return (
        <g>
          <circle cx="90" cy="36" r="14" fill="#e8e4d0" opacity="0.85" />
          <rect y="135" width="600" height="25" fill="#0c140f" />
          {Array.from({ length: 14 }, (_, i) => {
            const x = i * 46 - 10
            const h = 70 + ((i * 37) % 40)
            return (
              <g key={i}>
                <rect x={x + 18} y={135 - 14} width="8" height="14" fill="#2a1d16" />
                <polygon points={`${x},${135 - 12} ${x + 22},${135 - 12 - h} ${x + 44},${135 - 12}`} fill={i % 2 ? '#1c3324' : '#24402e'} />
              </g>
            )
          })}
        </g>
      )
    case 'cave':
      return (
        <g>
          <path d="M0 160 V40 Q300 -30 600 40 V160 Z" fill="#2a221a" />
          <path d="M120 160 V90 Q300 20 480 90 V160 Z" fill="#08070a" />
          {[150, 200, 260, 330, 390, 440].map((x, i) => (
            <polygon key={x} points={`${x},${44 - (i % 3) * 4} ${x + 10},${44 - (i % 3) * 4} ${x + 5},${70 + (i % 2) * 14}`} fill="#3a3024" />
          ))}
          <circle cx="96" cy="88" r="34" fill="#e3a857" opacity="0.18" />
          <rect x="93" y="88" width="6" height="30" fill="#4a3020" />
          <polygon points="90,88 102,88 96,70" fill="#e3a857" />
        </g>
      )
    case 'castle':
      return (
        <g>
          <circle cx="480" cy="34" r="12" fill="#e8e4d0" opacity="0.8" />
          <rect y="138" width="600" height="22" fill="#1a1a2a" />
          <rect x="190" y="70" width="220" height="68" fill="#3a3a52" />
          {[150, 400].map((x) => (
            <g key={x}>
              <rect x={x} y="40" width="50" height="98" fill="#2e2e44" />
              {[0, 1, 2].map((k) => <rect key={k} x={x + k * 18} y="32" width="12" height="10" fill="#2e2e44" />)}
              <rect x={x + 19} y="64" width="12" height="18" fill="#e3a857" />
            </g>
          ))}
          {[0, 1, 2, 3, 4, 5, 6, 7, 8].map((k) => <rect key={k} x={194 + k * 24} y="62" width="14" height="10" fill="#3a3a52" />)}
          <path d="M280 138 V108 Q300 90 320 108 V138 Z" fill="#14141f" />
          <line x1="175" y1="40" x2="175" y2="12" stroke="#b7a58f" strokeWidth="2" />
          <polygon points="175,12 196,18 175,24" fill="#b33a2a" />
        </g>
      )
    case 'temple':
      return (
        <g>
          <circle cx="300" cy="80" r="90" fill="#f3d9a0" opacity="0.12" />
          <rect y="140" width="600" height="20" fill="#3a2e2a" />
          <rect x="170" y="128" width="260" height="12" fill="#cbb89a" />
          <rect x="185" y="118" width="230" height="10" fill="#d9c8aa" />
          {[200, 245, 290, 335, 380].map((x) => <rect key={x} x={x} y="58" width="20" height="60" fill="#e6d8bc" />)}
          <rect x="185" y="48" width="230" height="10" fill="#d9c8aa" />
          <polygon points="180,48 300,14 420,48" fill="#cbb89a" />
          <circle cx="300" cy="36" r="6" fill="#e3a857" />
        </g>
      )
    default: // road
      return (
        <g>
          <circle cx="470" cy="46" r="18" fill="#f3d9a0" opacity="0.9" />
          <path d="M0 110 Q150 70 300 100 T600 90 V160 H0 Z" fill="#5a6a4a" />
          <path d="M0 130 Q200 100 340 120 T600 115 V160 H0 Z" fill="#3f4a32" />
          <path d="M250 160 Q300 128 330 112 Q350 104 372 102 L380 104 Q356 112 344 124 Q320 146 330 160 Z" fill="#b9956a" />
          <rect x="140" y="92" width="5" height="40" fill="#4a3020" />
          <polygon points="120,96 168,96 176,104 168,112 120,112" fill="#8a6a4a" />
        </g>
      )
  }
}
