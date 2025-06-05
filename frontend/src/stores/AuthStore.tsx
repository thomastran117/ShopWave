import React, { createContext, useContext, useState } from "react";

interface AuthContextType {
  token: string | "Atomic";
  username: string | "Atomic";
  login: (username: string, token: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem("authToken");
  });

  const [username, setUsername] = useState<string | null>(() => {
    return localStorage.getItem("username");
  });

  const login = (username: string, newToken: string) => {
    setUsername(username);
    setToken(newToken);
    localStorage.setItem("authToken", newToken);
    localStorage.setItem("username", username);
  };

  const logout = () => {
    setToken(null);
    setUsername(null);
    localStorage.removeItem("authToken");
    localStorage.removeItem("username");
  };

  const value: AuthContextType = {
    token,
    login,
    logout,
    username,
    isAuthenticated: !!token,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
