import React from "react";
import { FaGithub, FaTwitter, FaLinkedin, FaEnvelope } from "react-icons/fa";

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-[#212529] text-gray-300 pt-8 pb-6 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-wrap -mx-4 mb-8">
          {/* ClubXperience Info */}
          <div className="w-full md:w-1/3 px-4 mb-8 md:mb-0">
            <h5 className="uppercase border-b border-gray-700 pb-2 mb-4 flex items-center text-lg font-semibold">
              <i className="fas fa-ghost me-2"></i>
              ShopWave
            </h5>
            <p className="text-gray-400">
              A modern, dark-themed UI component library for React applications.
            </p>
            <div className="flex space-x-4 mt-4 text-gray-400">
              <a
                href="#"
                aria-label="Github"
                className="hover:text-white transition"
              >
                <FaGithub size={20} />
              </a>
              <a
                href="#"
                aria-label="Twitter"
                className="hover:text-white transition"
              >
                <FaTwitter size={20} />
              </a>
              <a
                href="#"
                aria-label="LinkedIn"
                className="hover:text-white transition"
              >
                <FaLinkedin size={20} />
              </a>
              <a
                href="#"
                aria-label="Email"
                className="hover:text-white transition"
              >
                <FaEnvelope size={20} />
              </a>
            </div>
          </div>

          {/* Links */}
          <div className="w-full sm:w-1/2 md:w-1/6 px-4 mb-8 md:mb-0">
            <h5 className="uppercase border-b border-gray-700 pb-2 mb-4 text-lg font-semibold">
              Links
            </h5>
            <ul className="space-y-2">
              {["Home", "Features", "Pricing", "About"].map((link) => (
                <li key={link}>
                  <a
                    href="#"
                    className="text-gray-400 hover:text-white transition"
                  >
                    {link}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Resources */}
          <div className="w-full sm:w-1/2 md:w-1/5 px-4 mb-8 md:mb-0">
            <h5 className="uppercase border-b border-gray-700 pb-2 mb-4 text-lg font-semibold">
              Resources
            </h5>
            <ul className="space-y-2">
              {["Documentation", "Tutorials", "Blog", "Support"].map((res) => (
                <li key={res}>
                  <a
                    href="#"
                    className="text-gray-400 hover:text-white transition"
                  >
                    {res}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Newsletter */}
          <div className="w-full md:w-1/4 px-4">
            <h5 className="uppercase border-b border-gray-700 pb-2 mb-4 text-lg font-semibold">
              Newsletter
            </h5>
            <p className="text-gray-400 mb-4">
              Subscribe to our newsletter for updates.
            </p>
            <form className="flex">
              <input
                type="email"
                placeholder="Your email"
                className="flex-grow px-3 py-2 rounded-l-md bg-gray-800 border border-gray-700 text-gray-300 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:border-transparent"
                aria-label="Email"
              />
              <button
                type="submit"
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-r-md transition"
              >
                Subscribe
              </button>
            </form>
          </div>
        </div>

        <div className="border-t border-gray-700 pt-4">
          <p className="text-center text-gray-500 text-sm">
            &copy; {currentYear} ClubXperience. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
