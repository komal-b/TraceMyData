// components/PrivateRoute.tsx
import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';

interface Props {
  children: ReactNode;
}

export default function PrivateRoute({ children }: Props) {
  const user = localStorage.getItem('user');

  
   const isAuthenticated = user !== null;

  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}
