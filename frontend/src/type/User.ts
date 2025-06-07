// types/User.ts
export interface User {
  email: string;
  firstName: string;
  lastName: string;
  profilePic?: string;
  authProvider: string;
  token: string; // Optional, if you want to include token in user data
}
