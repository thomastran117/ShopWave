import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../styles/login.css";

const images = ["/carousel1.jpg", "/carousel2.jpg", "/carousel3.jpg"];

const api = axios.create({
  baseURL: "http://localhost:8090/api",
});

export default function LoginPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ username: "", password: "" });
  const [slide, setSlide] = useState(0);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await api.post("/auth/login", {
        email: formData.username,
        password: formData.password,
      });
      localStorage.setItem("token", res.data.token);
      navigate("/dashboard");
    } catch (err) {
      console.error("Login failed", err);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleResponse = async (response: any) => {
    setLoading(true);
    try {
      const idToken = response.credential;
      const res = await api.post("/auth/google", { idToken });
      localStorage.setItem("token", res.data.accessToken);
      navigate("/dashboard");
    } catch (err) {
      console.error("Google login failed", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const interval = setInterval(() => {
      setSlide((prev) => (prev + 1) % images.length);
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (window.google) {
      window.google.accounts.id.initialize({
        client_id:
          "770432614028-91g7hj1jsvdsqn60m7hv9baqued0k1lf.apps.googleusercontent.com",
        callback: handleGoogleResponse,
      });

      window.google.accounts.id.renderButton(
        document.getElementById("googleLoginBtn"),
        { theme: "filled_black", size: "large", shape: "pill" }
      );
    }
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#0a0f1c] via-[#111827] to-[#0d1a2f] flex items-center justify-center px-4 py-10">
      <div
        className="w-full max-w-4xl h-[700px] 
          bg-[#0D1B2A]/90 backdrop-blur-xl 
          rounded-3xl shadow-[0_0_25px_rgba(0,115,255,0.3)] 
          border border-[#1E3A5F]/70 
          flex flex-col md:flex-row"
      >
        <div className="md:w-1/2 flex flex-col justify-between p-4">
          <div className="relative w-full h-full overflow-hidden bg-black rounded-2xl">
            <img
              key={slide}
              src={images[slide]}
              alt={`Slide ${slide}`}
              className="object-cover w-full h-full rounded-2xl fade-in-zoom"
            />
            <div className="absolute top-0 left-0 w-full p-4 flex justify-between items-center z-10">
              <span className="text-xl font-bold text-white">AMU</span>
              <button className="text-sm border border-gray-500 px-3 py-1 rounded-md hover:bg-gray-800 text-white">
                Back to website
              </button>
            </div>

            <div className="absolute bottom-6 w-full text-center text-white z-10">
              <p className="font-semibold text-lg">
                Capturing Moments, Creating Memories
              </p>
              <div className="mt-4 flex justify-center space-x-2">
                {images.map((_, idx) => (
                  <button
                    key={idx}
                    onClick={() => setSlide(idx)}
                    className={`h-2 rounded-full transition-all duration-300 ${
                      slide === idx ? "w-8 bg-blue-400" : "w-2 bg-gray-500"
                    }`}
                    aria-label={`Go to slide ${idx + 1}`}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="md:w-1/2 p-8 flex flex-col justify-center">
          <div className="w-full">
            <h2 className="text-3xl font-bold mt-4 mb-2 text-center text-blue-400">
              Login to your account
            </h2>
            <p className="text-sm text-gray-400 mb-8 text-center">
              Don&apos;t have an account?{" "}
              <a href="/signup" className="text-blue-400 hover:underline">
                Sign up
              </a>
            </p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="flex items-center border border-gray-600 rounded-md bg-[#1e293b] px-3 py-2 focus-within:border-blue-500 transition">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-5 w-5 text-gray-400 mr-3"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5.121 17.804A9.97 9.97 0 0112 15c2.21 0 4.24.722 5.879 1.939M15 10a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>

                <input
                  type="text"
                  name="username"
                  placeholder="Email"
                  value={formData.username}
                  onChange={handleChange}
                  className="bg-transparent w-full focus:outline-none text-white placeholder-gray-400"
                  required
                />
              </div>

              <div className="flex items-center border border-gray-600 rounded-md bg-[#1e293b] px-3 py-2">
                <svg
                  className="h-5 w-5 text-gray-400 mr-3"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 11c-1.1 0-2 .9-2 2v2h4v-2c0-1.1-.9-2-2-2z"
                  />
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 9V7a6 6 0 1112 0v2M5 11h14v10H5z"
                  />
                </svg>

                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  placeholder="Password"
                  value={formData.password}
                  onChange={handleChange}
                  className="bg-transparent w-full focus:outline-none text-white"
                  required
                />

                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="ml-2 p-1 rounded-md transition duration-200 hover:bg-gray-700/50 focus:outline-none"
                >
                  {showPassword ? (
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      className="h-5 w-5 text-gray-400 hover:text-white transition"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M13.875 18.825A10.05 10.05 0 0112 19c-5.523 0-10-4.477-10-10a9.96 9.96 0 012.291-6.405m2.62-2.62A9.96 9.96 0 0112 1c5.523 0 10 4.477 10 10 0 2.071-.632 3.994-1.715 5.585m-3.082 3.082A9.96 9.96 0 0112 21c-1.05 0-2.065-.162-3.018-.463M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                    </svg>
                  ) : (
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      className="h-5 w-5 text-gray-400 hover:text-white transition"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                      />
                    </svg>
                  )}
                </button>
              </div>

              <button
                type="submit"
                disabled={loading}
                className={`w-full flex justify-center items-center gap-2 ${
                  loading
                    ? "bg-blue-500 cursor-not-allowed"
                    : "bg-blue-600 hover:bg-blue-700 hover:scale-[1.02] hover:shadow-[0_0_15px_rgba(0,115,255,0.6)]"
                } transition text-white font-semibold py-2 rounded-md shadow-md transform duration-200`}
              >
                {loading && (
                  <svg
                    className="animate-spin h-5 w-5 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    ></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v8H4z"
                    ></path>
                  </svg>
                )}
                {loading ? "Processing..." : "Login"}
              </button>
            </form>

            <div className="mt-8">
              <div className="flex items-center text-gray-500 mb-4">
                <hr className="flex-grow border-gray-700" />
                <span className="mx-4 text-sm">or continue with</span>
                <hr className="flex-grow border-gray-700" />
              </div>

              <div className="flex items-center justify-center space-x-4">
                <div id="googleLoginBtn"></div>
                <button className="flex-1 py-2 border border-gray-600 rounded-md flex items-center justify-center gap-2 hover:bg-gray-800 transition">
                  <img src="/apple-icon.png" alt="Apple" className="w-5 h-5" />
                  <span className="text-gray-300">Apple</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
