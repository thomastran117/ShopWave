import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

interface AuthState {
  accessToken: string | null;
  email: string | null;
  role: string | null;
}

const initialState: AuthState = {
  accessToken: null,
  email: null,
  role: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setCredentials: (
      state,
      action: PayloadAction<{ accessToken: string; email: string; role: string }>
    ) => {
      state.accessToken = action.payload.accessToken;
      state.email = action.payload.email;
      state.role = action.payload.role;
    },
    clearCredentials: (state) => {
      state.accessToken = null;
      state.email = null;
      state.role = null;
    },
  },
});

export const { setCredentials, clearCredentials } = authSlice.actions;
export default authSlice.reducer;