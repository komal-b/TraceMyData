// components/ProfileUpdateModal.tsx
import { useState } from 'react';
import { AiOutlineEye, AiOutlineEyeInvisible } from 'react-icons/ai';
import type { User } from '../type/User';

type ProfileUpdateProps = {
  open: boolean;
  onClose: () => void;
  user: User;
  token: string;
};

export default function ProfileUpdate({ open, onClose, user, token }: ProfileUpdateProps) {
  const [form, setForm] = useState({
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    newEmail: '',
    showChangeEmail: false,
    showChangePassword: false,
    oldPassword: '',
    newPassword: '',
    passwordVisible: false,
    confirmVisible: false
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSave = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/user/update-profile', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          firstName: form.firstName,
          lastName: form.lastName,
          newEmail: form.showChangeEmail ? form.newEmail : undefined,
        }),
      });

      if (res.status === 202) {
        alert('Verification email sent. Please check within 24 hours.');
      } else if (!res.ok) {
        throw new Error(await res.text());
      } else {
        alert('Profile updated successfully.');
        onClose();
      }
    } catch (error) {
      alert('Update failed: ' + error);
    }
  };

  const handlePasswordChange = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/user/change-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          oldPassword: form.oldPassword,
          newPassword: form.newPassword,
        }),
      });
      if (!res.ok) throw new Error(await res.text());
      alert('Password changed successfully.');
      setForm(f => ({ ...f, oldPassword: '', newPassword: '', showChangePassword: false }));
    } catch (err) {
      alert('Password update failed: ' + err);
    }
  };

  if (!open) return null;

  return (
    <div className="fixed inset-0  bg-opacity-80 backdrop-blur-sm flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl p-8 w-full max-w-xl text-gray-800 space-y-6 border border-gray-200 relative">
        <button onClick={onClose} className="absolute top-2 right-3 text-2xl text-gray-700">×</button>
        <h2 className="text-3xl font-bold text-center bg-gradient-to-r from-blue-500 to-purple-500 bg-clip-text text-transparent">
          Update Profile
        </h2>

        <input
          className="w-full p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
          type="text"
          name="firstName"
          placeholder="First Name"
          value={form.firstName}
          onChange={handleChange}
        />

        <input
          className="w-full p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
          type="text"
          name="lastName"
          placeholder="Last Name"
          value={form.lastName}
          onChange={handleChange}
        />

        <div className="relative">
          <input
            className="w-full p-3 rounded-lg border border-gray-300 disabled:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
            type="email"
            name="newEmail"
            value={form.showChangeEmail ? form.newEmail : user.email}
            onChange={handleChange}
            disabled={user.authProvider === 'google' || !form.showChangeEmail}
          />
          {user.authProvider === 'local' && !form.showChangeEmail && (
            <button
              type="button"
              onClick={() => setForm(f => ({ ...f, showChangeEmail: true }))}
              className="absolute right-2 top-3 text-sm text-blue-600 hover:underline"
            >
              Change Email
            </button>
          )}
        </div>

        {form.showChangeEmail && (
          <p className="text-sm text-yellow-600">
            Verify your new email via the link sent (valid for 24 hours)
          </p>
        )}

        <div className="relative">
          <input
            className="w-full p-3 rounded-lg border bg-gray-100"
            type="password"
            value="********"
            disabled
          />
          {user.authProvider === 'local' && !form.showChangePassword && (
            <button
              type="button"
              onClick={() => setForm(f => ({ ...f, showChangePassword: true }))}
              className="absolute right-2 top-3 text-sm text-blue-600 hover:underline"
            >
              Change Password
            </button>
          )}
        </div>

        {form.showChangePassword && (
          <div className="space-y-3">
            <div className="relative">
              <input
                className="w-full p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
                type={form.passwordVisible ? 'text' : 'password'}
                name="oldPassword"
                value={form.oldPassword}
                onChange={handleChange}
                placeholder="Old Password"
              />
              <span
                onClick={() => setForm(f => ({ ...f, passwordVisible: !f.passwordVisible }))}
                className="absolute right-3 top-3 cursor-pointer"
              >
                {form.passwordVisible ? <AiOutlineEye /> : <AiOutlineEyeInvisible />}
              </span>
            </div>

            <div className="relative">
              <input
                className="w-full p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
                type={form.confirmVisible ? 'text' : 'password'}
                name="newPassword"
                value={form.newPassword}
                onChange={handleChange}
                placeholder="New Password"
              />
              <span
                onClick={() => setForm(f => ({ ...f, confirmVisible: !f.confirmVisible }))}
                className="absolute right-3 top-3 cursor-pointer"
              >
                {form.confirmVisible ? <AiOutlineEye /> : <AiOutlineEyeInvisible />}
              </span>
            </div>

            <button
              onClick={handlePasswordChange}
              className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white p-3 rounded-lg font-semibold shadow-md"
            >
              Update Password
            </button>
          </div>
        )}

        <div className="flex justify-end gap-3 pt-4">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
          >
            Cancel
          </button>
          <button
            onClick={handleSave}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Save Changes
          </button>
        </div>
      </div>
    </div>
  );
}
