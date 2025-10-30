import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function JoinPage() {
  const [contestId, setContestId] = useState('')
  const [username, setUsername] = useState('')
  const [error, setError] = useState('')
  const navigate = useNavigate()

  async function onSubmit(e) {
    e.preventDefault();
    const id = contestId.trim();
    const name = username.trim();
    if (!id || !name) {
      setError('Please provide both Contest ID and Username.');
      return;
    }
    try {
      const res = await fetch('/api/users/join', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: name }),
      });
      if (!res.ok) throw new Error('Failed to join contest');
      const data = await res.json();
      localStorage.setItem('contestId', id);
      localStorage.setItem('username', data.username);
      localStorage.setItem('userId', data.id);
      navigate(`/contest/${id}`);
    } catch (err) {
      setError(err.message);
    }
  }
  

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-md bg-white shadow rounded p-6">
        <h1 className="text-2xl font-bold mb-2">Join Contest</h1>
        <p className="text-sm text-gray-500 mb-4">Enter the contest ID and your username to continue.</p>
        {error && (
          <div className="mb-3 text-sm text-red-600">{error}</div>
        )}
        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Contest ID</label>
            <input
              value={contestId}
              onChange={e => setContestId(e.target.value)}
              className="mt-1 w-full border rounded px-3 py-2 focus:outline-none focus:ring focus:border-blue-300"
              placeholder="enter contest id"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Username</label>
            <input
              value={username}
              onChange={e => setUsername(e.target.value)}
              className="mt-1 w-full border rounded px-3 py-2 focus:outline-none focus:ring focus:border-blue-300"
              placeholder="your username"
            />
          </div>
          <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">Join</button>
        </form>
      </div>
    </div>
  )
}
