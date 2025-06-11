// ProfileUpdate.tsx (Your current code, largely correct for its part)
import { useState, useEffect } from 'react'; // <-- Ensure useEffect is imported
import { AiOutlineEye, AiOutlineEyeInvisible } from 'react-icons/ai';
import type { User } from '../type/User';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { logout } from '../utils/Logout';
import { useNavigate } from 'react-router-dom';

type ProfileUpdateProps = {
  open: boolean;
  onClose: () => void;
  user: User;
  token: string;
  onUserUpdate: (updatedUser: User) => void;
};

export default function ProfileUpdate({ open, onClose, user, onUserUpdate, token }: ProfileUpdateProps) {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email, // This is the email that was passed in the prop
    newEmail: '',
    showChangeEmail: false,
    showChangePassword: false,
    oldPassword: '',
    newPassword: '',
    passwordVisible: false,
    confirmVisible: false,
  });

  // UseEffect to re-sync form state when the 'user' prop changes
  // This handles cases where the user prop changes *while the modal is open*
  // or when it re-mounts with an updated user prop from the parent.
  useEffect(() => {
    setForm({
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      newEmail: '', // Reset newEmail when user prop updates
      showChangeEmail: false,
      showChangePassword: false,
      oldPassword: '',
      newPassword: '',
      passwordVisible: false,
      confirmVisible: false,
    });
  }, [user]); // Depend on the 'user' prop

  const [emailError, setEmailError] = useState<string | null>(null);

  const validateEmail = (email: string) => {
    const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!pattern.test(email)) {
      setEmailError('Please enter a valid email address.');
      return false;
    } else {
      setEmailError(null);
      return true;
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));

    if (name === 'newEmail') {
      validateEmail(value);
    }
  };

  const handleSave = async () => {
    if (form.showChangeEmail && !validateEmail(form.newEmail)) {
      toast.error('Invalid email.');
      return;
    }

    try {
      const res = await fetch('http://localhost:8080/api/auth/update-profile', {
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
      const data = await res.json();

      if (!res.ok) {
        toast.error(data.error);
      } else {
        // Construct the updated user object based on the response
        const updatedUser: User = {
          ...user, // Spread existing user properties to preserve those not updated
          firstName: data.user.firstName,
          lastName: data.user.lastName,
          email: data.user.email,
          profilePic: data.user.profilePic || '',
          authProvider: data.user.authProvider,
          token: data.user.token, // Ensure the token is passed through if it's in the response
        };

        localStorage.setItem('user', JSON.stringify({ user: updatedUser }));
        onUserUpdate(updatedUser); // <--- THIS IS THE CRITICAL CALL TO UPDATE PARENT STATE

        if (data.message) {
          toast.success(data.message, {
            onClose: () => {
              onClose(); // Now close the modal after the parent state has been updated
            },
          });
        }
      }
    } catch (error) {
      toast.error('Update failed: ' + (error instanceof Error ? error.message : String(error)));
    }
  };

  const handlePasswordChange = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/auth/change-password', {
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
      const message = await res.text();
      if (!res.ok) {
        toast.error(message);
        return;
      }
      toast.success(message, {
        onClose: () => {
          logout(navigate);
          navigate('/login');
          alert('You have been logged out. Please log in again with new password.');
        },
      });
    } catch (err) {
      toast.error('Password update failed: ' + (err as Error).message);
    }
  };

  if (!open) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-2xl shadow-xl p-6 w-full max-w-lg relative space-y-6">
          <button onClick={onClose} className="absolute top-2 right-3 text-3xl text-gray-500 hover:text-gray-700">×</button>

          <h2 className="text-3xl font-bold text-center bg-gradient-to-r from-blue-500 to-purple-500 bg-clip-text text-transparent">
            Update Profile
          </h2>

          <div className="space-y-3">
            <input
              className="w-full p-3 rounded-lg border border-gray-300 text-black focus:outline-none focus:ring-2 focus:ring-blue-500"
              type="text"
              name="firstName"
              placeholder="First Name"
              value={form.firstName}
              onChange={handleChange}
            />
            <input
              className="w-full p-3 rounded-lg border border-gray-300 text-black focus:outline-none focus:ring-2 focus:ring-blue-500"
              type="text"
              name="lastName"
              placeholder="Last Name"
              value={form.lastName}
              onChange={handleChange}
            />
            <div className="relative">
              <input
                className={`w-full p-3 rounded-lg focus:outline-none text-black focus:ring-2 focus:ring-blue-500 ${
                  emailError ? 'border-red-500' : 'border-gray-300 border'
                } disabled:bg-gray-100`}
                type="email"
                name="newEmail"
                value={form.showChangeEmail ? form.newEmail : form.email} // Use form.email here
                onChange={handleChange}
                disabled={user.authProvider === 'google' || !form.showChangeEmail}
              />
              {user.authProvider === 'local' && !form.showChangeEmail && (
                <button
                  type="button"
                  onClick={() => setForm((f) => ({ ...f, showChangeEmail: true, newEmail: '' }))} // Clear newEmail on click
                  className="absolute right-3 top-3 text-sm text-blue-600 hover:underline"
                >
                  Change Email
                </button>
              )}
            </div>
            {emailError && <p className="text-sm text-red-500">{emailError}</p>}

            {form.showChangeEmail && (
              <p className="text-sm text-yellow-600">
                A verification email will be sent. Valid for 24 hours.
              </p>
            )}
          </div>

          <div className="flex justify-end gap-3">
            <button
              onClick={onClose}
              className="px-4 py-2 rounded text-black bg-gray-300 hover:bg-gray-400"
            >
              Cancel
            </button>
            <button
              onClick={handleSave}
              className="px-4 py-2 rounded bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold hover:from-blue-700 hover:to-purple-700"
            >
              Save Changes
            </button>
          </div>

          {/* Password Section */}
          {user.authProvider === 'local' && (
            <>
              <hr className="border-gray-300" />
              <h3 className="text-xl font-semibold text-gray-800">Change Password</h3>

              <div className="relative mb-3">
                <input
                  className="w-full p-3 rounded-lg text-black border bg-gray-100"
                  type="password"
                  value="********"
                  disabled
                />
                {!form.showChangePassword && (
                  <button
                    type="button"
                    onClick={() => setForm((f) => ({ ...f, showChangePassword: true, oldPassword: '', newPassword: '' }))}
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
                      className="w-full p-3 rounded-lg border border-gray-300 text-black focus:outline-none focus:ring-2 focus:ring-blue-500"
                      type={form.passwordVisible ? 'text' : 'password'}
                      name="oldPassword"
                      value={form.oldPassword}
                      onChange={handleChange}
                      placeholder="Old Password"
                    />
                    <span
                      onClick={() => setForm((f) => ({ ...f, passwordVisible: !f.passwordVisible }))}
                      className="absolute right-3 text-blue-600 top-3 cursor-pointer"
                    >
                      {form.passwordVisible ? <AiOutlineEye /> : <AiOutlineEyeInvisible />}
                    </span>
                  </div>

                  <div className="relative">
                    <input
                      className="w-full p-3 rounded-lg border border-gray-300 text-black focus:outline-none focus:ring-2 focus:ring-blue-500"
                      type={form.confirmVisible ? 'text' : 'password'}
                      name="newPassword"
                      value={form.newPassword}
                      onChange={handleChange}
                      placeholder="New Password"
                    />
                    <span
                      onClick={() => setForm((f) => ({ ...f, confirmVisible: !f.confirmVisible }))}
                      className="absolute right-3 top-3 text-blue-600 cursor-pointer"
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
            </>
          )}
        </div>
      </div>

      <ToastContainer position="top-right" autoClose={3000} aria-label={undefined} />
    </>
  );
}