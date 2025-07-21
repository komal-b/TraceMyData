import { useEffect, useState } from "react";

interface WebsiteMetadata {
  url: string;
  title?: string;
  description?: string;
  favicon?: string;
}

interface TrackerDetection {
  trackerName: string;
  riskLevel: string;
  requestUrlsJson: string;
  scriptSnippets: string;
}

interface TrackerDetectionResponse {
  metadata: WebsiteMetadata;
  detectedTrackers: TrackerDetection[];
}

export default function TrackerDetection() {
  const [trackerHistory, setTrackerHistory] = useState<TrackerDetectionResponse[]>([]);
  const [latestResult, setLatestResult] = useState<TrackerDetectionResponse | null>(null);
  const [url, setUrl] = useState("");
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 3;
  const [copiedIndex, setCopiedIndex] = useState<number | null>(null);

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

  const fetchTrackerDetections = async () => {
    setLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/tracker-detection/history", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${getToken()}`,
          Accept: "application/json",
        },
      });
      if (!response.ok) throw new Error("Failed to fetch tracker data");
      const data: TrackerDetectionResponse[] = await response.json();
      setTrackerHistory(data || []);
    } catch (err) {
      console.error(err);
      setError("Could not load tracker detections.");
    } finally {
      setLoading(false);
    }
  };

  const handleScan = async () => {
    if (!url.trim()) return;
    setSearching(true);
    setError(null);
    setLatestResult(null);

    try {
      const response = await fetch(
        `http://localhost:8080/api/tracker-detection/detect?url=${encodeURIComponent(url)}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${getToken()}`,
            Accept: "application/json",
          },
        }
      );
      if (!response.ok) throw new Error("Failed to fetch detection result");
      const data: TrackerDetectionResponse = await response.json();
      setLatestResult(data);
      await fetchTrackerDetections();
    } catch (err) {
      console.error(err);
      setError("Failed to scan tracker detection for the entered URL.");
    } finally {
      setSearching(false);
    }
  };

  useEffect(() => {
    fetchTrackerDetections();
  }, []);

  const getBadgeColor = (risk: string): string => {
    switch (risk.toLowerCase()) {
      case "high":
        return "bg-red-500";
      case "medium":
        return "bg-yellow-400";
      case "low":
        return "bg-green-500";
      default:
        return "bg-gray-400";
    }
  };

  const renderTrackerCard = (tracker: TrackerDetection, index: number) => {
    let urls: string[] = [];
    try {
      urls = JSON.parse(tracker.requestUrlsJson || "[]");
    } catch {
      urls = [tracker.requestUrlsJson];
    }

    const handleCopy = (index: number, p0: string) => {
      if (tracker.scriptSnippets) {
        navigator.clipboard.writeText(tracker.scriptSnippets);
        setCopiedIndex(index);
        setTimeout(() => setCopiedIndex(null), 1500);
      }
    };

    return (
      <div
        key={index}
        className="w-full border border-gray-300 bg-white rounded-md p-4 sm:p-6 shadow-sm overflow-hidden break-words"
      >
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-lg font-semibold text-gray-800">
            🕵️ {tracker.trackerName}
          </h2>
          <span
            className={`px-2 py-1 text-xs font-bold text-white rounded ${getBadgeColor(
              tracker.riskLevel
            )}`}
          >
            {tracker.riskLevel.toUpperCase()}
          </span>
        </div>

        <div className="text-sm text-gray-700 mb-3">
          <strong>Requested URLs:</strong>
          <ul className="mt-2 space-y-1 bg-gray-100 border border-gray-200 p-3 rounded-md text-xs max-h-60 overflow-y-auto overflow-x-hidden break-all">
            {urls.map((url, i) => (
              <li key={i} className="break-words text-blue-900">{url}</li>
            ))}
          </ul>
        </div>

        <details className="mt-2">
          <summary className="text-blue-600 cursor-pointer text-sm font-medium">
            View Script Snippet
          </summary>

          <div className="relative rounded-md border border-gray-300 bg-gray-900 overflow-hidden mt-2">
            <div className="absolute top-2 left-2 z-10">
              <button
                className="text-white text-xs bg-gray-700 hover:bg-gray-600 px-3 py-1 rounded shadow"
                onClick={() => handleCopy(index, tracker.scriptSnippets || "")}
              >
                {copiedIndex === index ? "Copied" : "Copy"}
              </button>
            </div>

            <div
              className="p-3 pt-12 max-h-64 overflow-auto break-all"
              style={{
                whiteSpace: "pre-wrap",
                wordBreak: "break-word",
                overflowWrap: "anywhere",
              }}
            >
              <code className="text-green-200 text-xs font-mono block">
                {tracker.scriptSnippets || "N/A"}
              </code>
            </div>
          </div>
        </details>
      </div>
    );
  };

  const renderSiteHeader = (metadata: WebsiteMetadata) => (
    <div className="mb-2 flex items-center gap-2">
      {metadata.favicon ? (
        <img
          src={metadata.favicon}
          className="w-5 h-5"
          alt="favicon"
          onError={(e) => (e.currentTarget.style.display = "none")}
        />
      ) : (
        <span className="w-5 h-5 inline-block bg-gray-300 rounded" />
      )}
      <h3 className="text-lg font-semibold text-gray-800 break-words">{metadata.url || "Unknown Website"}</h3>
    </div>
  );

  const totalPages = Math.ceil(trackerHistory.length / itemsPerPage);
  const paginatedHistory = trackerHistory.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const renderPagination = () => (
    <div className="flex flex-wrap justify-center items-center mt-6 gap-2 text-sm">
      <button
        onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
        disabled={currentPage === 1}
        className="px-3 py-1 rounded bg-gray-500 hover:bg-gray-300 disabled:opacity-50"
      >
        Previous
      </button>
      <span className="px-4 py-1 text-gray-700">
        Page {currentPage} of {totalPages}
      </span>
      <button
        onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
        disabled={currentPage === totalPages}
        className="px-3 py-1 rounded bg-gray-500 hover:bg-gray-300 disabled:opacity-50"
      >
        Next
      </button>
    </div>
  );

  return (
    <div className="p-4 md:p-8">
      <div className="flex flex-col sm:flex-row gap-2 mb-6">
        <input
          type="url"
          placeholder="Enter website URL..."
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          className="flex-1 p-3 rounded border border-gray-300 focus:outline-none focus:ring focus:ring-blue-300 text-gray-900"
        />
        <button
          onClick={handleScan}
          className="px-4 py-2 rounded bg-gradient-to-r from-blue-500 to-purple-600 text-white hover:from-blue-600 hover:to-purple-700"
          disabled={searching}
        >
          {searching ? "Scanning..." : "Scan"}
        </button>
      </div>

      {loading && (
        <p className="text-gray-500 animate-pulse">Loading tracker history...</p>
      )}
      {error && <p className="text-red-600 mb-4">{error}</p>}

      {latestResult && (
        <div className="mb-10">
          <h2 className="text-2xl font-semibold mb-4">Latest Scan Result</h2>
          {renderSiteHeader(latestResult.metadata)}
          <div className="flex flex-col gap-6">
            {latestResult.detectedTrackers.map((tracker, index) =>
              renderTrackerCard(tracker, index)
            )}
          </div>
        </div>
      )}

      <h2 className="text-xl font-semibold mb-4">Detection History</h2>
      {trackerHistory.length > 0 ? (
        <>
          <div className="space-y-10">
            {paginatedHistory.map((entry, i) => (
              <div key={i}>
                {renderSiteHeader(entry.metadata)}
                <div className="flex flex-col gap-6">
                  {entry.detectedTrackers.map((tracker, index) =>
                    renderTrackerCard(tracker, index)
                  )}
                </div>
              </div>
            ))}
          </div>
          {renderPagination()}
        </>
      ) : (
        !loading && (
          <p className="text-gray-500">No tracker detections available.</p>
        )
      )}
    </div>
  );
}
