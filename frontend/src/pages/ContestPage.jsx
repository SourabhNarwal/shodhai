import { useEffect, useMemo, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import Editor from '@monaco-editor/react'
import Leaderboard from '../components/Leaderboard.jsx'

export default function ContestPage() {
  const { id } = useParams()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [contest, setContest] = useState(null)
  const [selectedProblemId, setSelectedProblemId] = useState('')
  const [code, setCode] = useState('// Write your solution here\nclass Solution {\n    public static void main(String[] args) {\n        System.out.println("hello world");\n    }\n}\n')
  const [submitting, setSubmitting] = useState(false)
  const [submissionId, setSubmissionId] = useState('')
  const [submissionStatus, setSubmissionStatus] = useState('')
  const pollRef = useRef(null)

  useEffect(() => {
    let cancelled = false
    async function fetchContest() {
      setLoading(true)
      setError('')
      try {
        const res = await fetch(`/api/contests/${id}`)
        if (!res.ok) throw new Error(`Failed to load contest (${res.status})`)
        const data = await res.json()
        if (!cancelled) {
          setContest(data)
          setSelectedProblemId(data?.problems?.[0]?.id || '')
        }
      } catch (e) {
        if (!cancelled) setError(e.message || 'Failed to load contest')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    fetchContest()
    return () => { cancelled = true }
  }, [id])

  useEffect(() => {
    return () => {
      if (pollRef.current) {
        clearInterval(pollRef.current)
        pollRef.current = null
      }
    }
  }, [])

  const selectedProblem = useMemo(() => {
    if (!contest?.problems) return null
    return contest.problems.find(p => p.id === selectedProblemId) || contest.problems[0] || null
  }, [contest, selectedProblemId])

  async function handleSubmit() {
    if (!selectedProblem) return
    const username = localStorage.getItem('username') || ''
    const userId = localStorage.getItem('userId') || username // fallback
    if (!userId) {
      alert('Please join the contest with a username first.')
      return
    }
    setSubmitting(true)
    setSubmissionStatus('')
    setSubmissionId('')
    try {
      const res = await fetch('/api/submissions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId,
          problemId: selectedProblem.id,
          code,
          language: 'java',
        }),
      })
      if (!res.ok) throw new Error('Failed to submit')
      const data = await res.json()
      const sid = data?.submissionId
      setSubmissionId(sid)
      setSubmissionStatus('Pending')
      if (pollRef.current) clearInterval(pollRef.current)
      pollRef.current = setInterval(async () => {
        try {
          const sres = await fetch(`/api/submissions/${sid}`)
          if (!sres.ok) return
          const sdata = await sres.json()
          setSubmissionStatus(sdata?.status || '')
          if (sdata?.status === 'Accepted' || sdata?.status === 'Wrong Answer' || sdata?.status === 'Error') {
            clearInterval(pollRef.current)
            pollRef.current = null
          }
        } catch {}
      }, 3000)
    } catch (e) {
      setError(e.message || 'Submission failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-4">
        {/* Left: Problems */}
        <div className="lg:col-span-3 space-y-4">
          <div className="bg-white rounded shadow p-4">
            <h2 className="text-xl font-semibold">Contest</h2>
            {loading ? (
              <p className="text-gray-500">Loading...</p>
            ) : error ? (
              <p className="text-red-600 text-sm">{error}</p>
            ) : (
              <div>
                <p className="text-gray-700 font-medium">{contest?.name}</p>
              </div>
            )}
          </div>
          <div className="bg-white rounded shadow p-4">
            <h3 className="text-lg font-semibold mb-2">Problems</h3>
            <div className="space-y-2">
              {contest?.problems?.length ? contest.problems.map(p => (
                <button
                  key={p.id}
                  onClick={() => setSelectedProblemId(p.id)}
                  className={`w-full text-left px-3 py-2 rounded border ${selectedProblemId === p.id ? 'bg-blue-50 border-blue-300' : 'hover:bg-gray-50'}`}
                >
                  <div className="font-medium">{p.title}</div>
                </button>
              )) : (
                <p className="text-sm text-gray-500">No problems available.</p>
              )}
            </div>
          </div>
        </div>

        {/* Center: Editor + Problem description */}
        <div className="lg:col-span-6 space-y-4">
          <div className="bg-white rounded shadow p-4">
            <h2 className="text-xl font-semibold mb-2">Problem</h2>
            {selectedProblem ? (
              <div>
                <div className="text-lg font-medium mb-2">{selectedProblem.title}</div>
                <p className="text-gray-700 whitespace-pre-wrap">{selectedProblem.description}</p>
              </div>
            ) : (
              <p className="text-gray-500">Select a problem to view details.</p>
            )}
          </div>
          <div className="bg-white rounded shadow p-2">
            <div className="flex items-center justify-between px-2 py-1">
              <h2 className="text-lg font-semibold">Editor</h2>
              <div className="flex items-center gap-3">
                {submissionStatus && (
                  <span className={`text-sm ${submissionStatus === 'Accepted' ? 'text-green-700' : submissionStatus === 'Running' ? 'text-blue-700' : submissionStatus === 'Wrong Answer' || submissionStatus === 'Error' ? 'text-red-700' : 'text-gray-600'}`}>
                    {submissionStatus}
                  </span>
                )}
                <button
                  onClick={handleSubmit}
                  disabled={submitting || !selectedProblem}
                  className="px-4 py-1.5 rounded bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-60"
                >
                  {submitting ? 'Submitting...' : 'Submit'}
                </button>
              </div>
            </div>
            <div className="h-[420px]">
              <Editor
                height="100%"
                defaultLanguage="java"
                value={code}
                onChange={(v) => setCode(v ?? '')}
                options={{ fontSize: 14, minimap: { enabled: false } }}
              />
            </div>
          </div>
        </div>

        {/* Right: Leaderboard */}
        <div className="lg:col-span-3 space-y-4">
          <div className="bg-white rounded shadow p-4">
            <h2 className="text-xl font-semibold mb-3">Leaderboard</h2>
            <Leaderboard contestId={id} />
          </div>
        </div>
      </div>
    </div>
  )
}
