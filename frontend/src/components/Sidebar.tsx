import React, { useState } from "react";
import { FaHome, FaUserFriends, FaCalendarAlt, FaBookOpen, FaInfoCircle, FaChevronLeft, FaChevronRight,} from "react-icons/fa";
import { NavLink } from "react-router-dom";

const Sidebar: React.FC = () => {
  const [isExpanded, setIsExpanded] = useState(true);

  const toggleSidebar = () => {
    setIsExpanded(!isExpanded);
  };

  const navItems = [
    { label: "Home", icon: <FaHome />, to: "/" },
    { label: "Clubs", icon: <FaUserFriends />, to: "/clubs" },
    { label: "Events", icon: <FaCalendarAlt />, to: "/events" },
    { label: "Guide", icon: <FaBookOpen />, to: "/guide" },
    { label: "About", icon: <FaInfoCircle />, to: "/about" },
  ];

  return (
    <div
      className={`bg-gray-900 text-gray-200 h-screen p-4 shadow-lg flex flex-col transition-all duration-300 ${
        isExpanded ? "w-64" : "w-20"
      }`}
    >
      {/* Toggle Button */}
      <div className="flex justify-end mb-6">
        <button
          onClick={toggleSidebar}
          className="text-gray-400 hover:text-white transition"
        >
          {isExpanded ? <FaChevronLeft /> : <FaChevronRight />}
        </button>
      </div>

      {/* Navigation Links */}
      <nav className="flex flex-col space-y-2">
        {navItems.map(({ label, icon, to }) => (
          <NavLink
            key={label}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-4 px-3 py-2 rounded-lg hover:bg-gray-800 transition ${
                isActive ? "bg-gray-800 text-white" : ""
              }`
            }
          >
            <span className="text-lg">{icon}</span>
            {isExpanded && <span className="text-sm">{label}</span>}
          </NavLink>
        ))}
      </nav>

      {/* Footer or bottom buttons */}
      <div className="mt-auto pt-4 border-t border-gray-700 text-center text-xs text-gray-500">
        {isExpanded && <p>&copy; {new Date().getFullYear()} ClubXperience</p>}
      </div>
    </div>
  );
};

export default Sidebar;
