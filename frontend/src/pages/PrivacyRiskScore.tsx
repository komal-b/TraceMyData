import { useEffect, useState } from "react";
import { Circle } from "rc-progress"; // install via: npm install rc-progress

interface PrivacyRisk {
  url: string;
  riskScore: number;
  riskLabel: string;
  riskTags?: string;
}


export default function PrivacyRiskScore() {
  const [risks, setRisks] = useState<PrivacyRisk[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const getToken = (): string => {
    const stored = localStorage.getItem("user");
    if (stored) {
      try {
        return JSON.parse(stored).user.token || "";
      } catch {
        return "";
      }
    }
    return "";
  };

  const fetchRiskScores = async () => {
    setLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/privacy-risk/score", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${getToken()}`,
          Accept: "application/json",
        },
      });

      if (!response.ok) throw new Error("Failed to fetch risk scores");
      const data: PrivacyRisk[] = await response.json();
      setRisks(data || []);
    } catch (err) {
      console.error(err);
      setError("Could not load risk data.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRiskScores();
  }, []);

  const getColor = (score: number) => {
    if (score >= 60) return "#DC2626"; // red
    if (score >= 25) return "#FACC15"; // yellow
    return "#16A34A"; // green
  };

  return (
  <div className="p-4 sm:p-6 md:p-8">

    {loading && <p className="text-gray-500 text-center">Loading risk scores...</p>}
    {error && <p className="text-red-500 text-center">{error}</p>}

    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
      {risks.map((risk, idx) => {
        const color = getColor(risk.riskScore);
        return (
          <div
            key={idx}
            className="bg-white rounded-xl shadow-md p-4 sm:p-6 border border-gray-200 hover:shadow-lg transition-all duration-300"
          >
            <div className="flex items-center justify-between mb-3 sm:mb-4">
              <h2 className="text-sm sm:text-md font-semibold text-gray-800 break-words break-all whitespace-pre-wrap w-full overflow-hidden">

                {risk.url}
              </h2>
            </div>

            <div className="flex justify-center my-3 sm:my-4">
              <div className="relative w-20 h-20 sm:w-24 sm:h-24">
                <Circle
                  percent={risk.riskScore}
                  strokeWidth={8}
                  strokeColor={color}
                  trailWidth={8}
                  trailColor="#E5E7EB"
                />
                <div className="absolute inset-0 flex items-center justify-center text-base sm:text-lg font-semibold text-gray-800">
                  {risk.riskScore}
                </div>
              </div>
            </div>

            <div className="text-center mt-3 sm:mt-4">
              <span
                className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${
                  risk.riskLabel === "High"
                    ? "bg-red-100 text-red-700"
                    : risk.riskLabel === "Medium"
                    ? "bg-yellow-100 text-yellow-700"
                    : "bg-green-100 text-green-700"
                }`}
              >
                {risk.riskLabel}
              </span>
            </div>

            {risk.riskTags && (
              <div className="mt-3 text-sm text-center">
                <div className="font-semibold text-gray-700 mb-1">Tags:</div>
                <div className="flex flex-wrap justify-center gap-1 sm:gap-2">
                  {risk.riskTags.split(",").map((tag, i) => (
                    <span
                      key={i}
                      className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-700"
                    >
                      {tag.trim()}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        );
      })}
    </div>
  </div>
);
}