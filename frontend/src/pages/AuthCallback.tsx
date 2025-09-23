import { useEffect } from "react";

export default function AuthCallback() {
  useEffect(() => {
    const hash = new URLSearchParams(window.location.hash.substring(1));
    const idToken = hash.get("id_token");

    if (idToken && window.opener) {
      window.opener.postMessage(
        { type: "google_oauth_token", token: idToken },
        window.location.origin
      );
      window.close();
    }
  }, []);

  return (
    <div className="text-white flex items-center justify-center h-screen">
      <p>Finishing login…</p>
    </div>
  );
}
