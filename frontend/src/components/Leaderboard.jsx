import { useEffect, useRef, useState } from 'react'

export default function Leaderboard({ contestId }) {
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const pollRef = useRef(null)

  async function fetchLeaderboard(id) {
    try {
      setError('')
      const res = await fetch(`/api/contests/${id}/leaderboard`)
      if (!res.ok) throw new Error('Failed to load leaderboard')
      const data = await res.json()
      // Ensure sorted desc just in case
      const sorted = (data || []).slice().sort((a, b) => (b.totalScore || 0) - (a.totalScore || 0))
      setEntries(sorted)
    } catch (e) {
      setError(e.message || 'Failed to load leaderboard')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!contestId) return
    setLoading(true)
    fetchLeaderboard(contestId)
    if (pollRef.current) clearInterval(pollRef.current)
    pollRef.current = setInterval(() => fetchLeaderboard(contestId), 20000)
    return () => {
      if (pollRef.current) clearInterval(pollRef.current)
      pollRef.current = null
    }
  }, [contestId])

  return (
    <div>
      {loading ? (
        <p className="text-gray-500">Loading...</p>
      ) : error ? (
        <p className="text-red-600 text-sm">{error}</p>
      ) : entries.length === 0 ? (
        <p className="text-gray-500 text-sm">No entries yet.</p>
      ) : (
        <ul className="divide-y">
          {entries.map((e, idx) => (
            <li key={e.userId || idx} className="py-2 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="w-6 text-sm text-gray-500">{idx + 1}</span>
                <span className="font-medium">{e.username}</span>
              </div>
              <span className="text-sm font-semibold">{e.totalScore}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
