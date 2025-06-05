// src/pages/LoginPage.tsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const images = ["/carousel1.jpg", "/carousel2.jpg", "/carousel3.jpg"];

export default function LoginPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ username: "", password: "" });
  const [slide, setSlide] = useState(0);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    navigate("/dashboard");
  };

  useEffect(() => {
    const interval = setInterval(() => {
      setSlide((prev) => (prev + 1) % images.length);
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#1f1d2b] via-[#2e2c3a] to-[#0f0e17] flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-4xl h-[700px] bg-[#1F1D2B]/70 backdrop-blur-lg text-white rounded-3xl overflow-hidden shadow-2xl flex flex-col md:flex-row">
        {/* Left - Carousel */}
        <div className="md:w-1/2 flex flex-col justify-between p-4">
          <div className="relative w-full h-full overflow-hidden bg-black rounded-2xl">
            <img
              key={slide}
              src={images[slide]}
              alt={`Slide ${slide}`}
              className="object-cover w-full h-full rounded-2xl transition-opacity duration-700 ease-in-out"
            />
            <div className="absolute top-0 left-0 w-full p-4 flex justify-between items-center z-10">
              <span className="text-xl font-bold text-white">AMU</span>
              <button className="text-sm border border-gray-400 px-3 py-1 rounded-md hover:bg-gray-700 text-white">
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
                      slide === idx ? "w-8 bg-white" : "w-2 bg-gray-500"
                    }`}
                    aria-label={`Go to slide ${idx + 1}`}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Right - Login Form */}
        <div className="md:w-1/2 p-8 flex flex-col justify-center">
          <div className="w-full">
            <h2 className="text-3xl font-bold mt-4 mb-2 text-center">
              Login to your account
            </h2>
            <p className="text-sm text-gray-400 mb-8 text-center">
              Don&apos;t have an account?{" "}
              <a href="/signup" className="text-purple-400 hover:underline">
                Sign up
              </a>
            </p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="flex items-center border border-gray-600 rounded-md bg-[#1F1D2B] px-3 py-2">
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
                    d="M5.121 17.804A9.97 9.97 0 0112 15c2.21 0 4.24.722 5.879 1.939M15 10a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
                <input
                  type="text"
                  name="username"
                  placeholder="Username"
                  value={formData.username}
                  onChange={handleChange}
                  className="bg-transparent w-full focus:outline-none text-white"
                  required
                />
              </div>

              <div className="flex items-center border border-gray-600 rounded-md bg-[#1F1D2B] px-3 py-2">
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
                  type="password"
                  name="password"
                  placeholder="Password"
                  value={formData.password}
                  onChange={handleChange}
                  className="bg-transparent w-full focus:outline-none text-white"
                  required
                />
              </div>

              <button
                type="submit"
                className="w-full bg-purple-700 hover:bg-purple-800 transition text-white font-semibold py-2 rounded-md"
              >
                Login
              </button>
            </form>

            <div className="mt-8">
              <div className="flex items-center text-gray-500 mb-4">
                <hr className="flex-grow border-gray-700" />
                <span className="mx-4 text-sm">or continue with</span>
                <hr className="flex-grow border-gray-700" />
              </div>

              <div className="flex items-center justify-center space-x-4">
                <button className="flex-1 py-2 border border-gray-600 rounded-md flex items-center justify-center gap-2 hover:bg-gray-700 transition">
                  <img
                    src="/google-icon.png"
                    alt="Google"
                    className="w-5 h-5"
                  />
                  Google
                </button>
                <button className="flex-1 py-2 border border-gray-600 rounded-md flex items-center justify-center gap-2 hover:bg-gray-700 transition">
                  <img src="/apple-icon.png" alt="Apple" className="w-5 h-5" />
                  Apple
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
