import { Routes, Route, Navigate } from 'react-router-dom'
import JoinPage from './pages/JoinPage.jsx'
import ContestPage from './pages/ContestPage.jsx'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/join" replace />} />
      <Route path="/join" element={<JoinPage />} />
      <Route path="/contest/:id" element={<ContestPage />} />
      <Route path="*" element={<Navigate to="/join" replace />} />
    </Routes>
  )
}
