// src/utils/logout.ts
export async function logout(navigate: Function) {
  try {
    await fetch("http://localhost:8080/api/auth/logout", {
      method: "POST",
      credentials: "include", // ensures cookies are sent
    });

    localStorage.removeItem("user"); // frontend data only
    navigate("/login");
  } catch (err) {
    console.error("Logout failed", err);
  }
}
