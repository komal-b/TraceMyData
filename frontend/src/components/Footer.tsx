// src/components/Footer.tsx
export default function Footer() {
  return (
    <footer className="bg-white">
      <div className="max-w-7xl mx-auto px-4 text-center text-gray-400">
        <p>&copy; {new Date().getFullYear()} TraceMyData. All rights reserved.</p>
      </div>
    </footer>
  );
}