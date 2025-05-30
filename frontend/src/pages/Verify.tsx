// src/pages/Verify.tsx
import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

export default function Verify() {
  const [status, setStatus] = useState('Verifying...');
  const [params] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const token = params.get('token');
    if (!token) return setStatus('Invalid verification link.');

    fetch(`/api/auth/verify?token=${token}`)
      .then(res => {
        if (!res.ok) throw new Error('Verification failed');
        return res.text();
      })
      .then(() => {
        setStatus('✅ Email verified! Redirecting to login...');
        setTimeout(() => navigate('/login'), 3000);
      })
      .catch(() => setStatus('❌ Verification failed or link expired.'));
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <h2 className="text-xl font-semibold">{status}</h2>
    </div>
  );
}
