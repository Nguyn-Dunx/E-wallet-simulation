import './PinPad.css';

const KEYS = ['1','2','3','4','5','6','7','8','9','','0','⌫'];

export default function PinPad({ value, onChange, maxLength = 6 }) {
  const handleKey = (k) => {
    if (k === '⌫') {
      onChange(value.slice(0, -1));
    } else if (k && value.length < maxLength) {
      onChange(value + k);
    }
  };

  return (
    <div className="pin-pad-container">
      {/* Display dots */}
      <div className="pin-dots">
        {Array.from({ length: maxLength }).map((_, i) => (
          <div
            key={i}
            className={`pin-dot ${i < value.length ? 'pin-dot--filled' : ''} ${i === value.length - 1 && value.length > 0 ? 'pin-dot--active' : ''}`}
          />
        ))}
      </div>

      {/* Keypad */}
      <div className="pin-keys">
        {KEYS.map((k, idx) => (
          <button
            key={idx}
            className={`pin-key ${!k ? 'pin-key--empty' : ''} ${k === '⌫' ? 'pin-key--delete' : ''}`}
            onClick={() => handleKey(k)}
            disabled={!k}
            type="button"
          >
            {k}
          </button>
        ))}
      </div>
    </div>
  );
}
