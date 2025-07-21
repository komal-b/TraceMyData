import { useState, useEffect } from "react";

interface Metadata {
  url: string;
  title: string | null;
  description: string | null;
  ogTitle: string | null;
  ogDescription: string | null;
  ogImage: string | null;
  favicon: string | null;
  createdAt: string;
}

export default function WebsiteMetadata() {
  const [url, setUrl] = useState("");
  const [history, setHistory] = useState<Metadata[]>([]);
  const [currentResult, setCurrentResult] = useState<Metadata | null>(null);
  const [loading, setLoading] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;
  const totalPages = Math.ceil(history.length / itemsPerPage);
  const paginatedHistory = history.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const handleNextPage = () => {
    if (currentPage < totalPages) setCurrentPage((prev) => prev + 1);
  };

  const handlePrevPage = () => {
    if (currentPage > 1) setCurrentPage((prev) => prev - 1);
  };

  const getToken = () => {
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

  const fetchHistory = async () => {
    setLoadingHistory(true);
    try {
      const response = await fetch(
        "http://localhost:8080/api/metadata/history",
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${getToken()}`,
            Accept: "application/json",
          },
        }
      );
      if (!response.ok) throw new Error("Failed to fetch history");
      const data: Metadata[] = await response.json();
      setHistory(data);
      setCurrentPage(1); // Reset to first page when history changes
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingHistory(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchMetadata = async () => {
    if (!url.trim()) return;

    setLoading(true);
    setError(null);
    setInfo(null);
    setCurrentResult(null);

    try {
      const response = await fetch(
        `http://localhost:8080/api/metadata/analyze?url=${encodeURIComponent(
          url
        )}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${getToken()}`,
            Accept: "application/json",
          },
        }
      );

      if (!response.ok) throw new Error("Failed to fetch metadata");
      const data: Metadata = await response.json();

      setCurrentResult(data);
      setInfo("Metadata fetched successfully.");
      await fetchHistory();
    } catch (err) {
      setError("Failed to fetch metadata. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-4 sm:p-6 md:p-8 max-w-5xl mx-auto">
      <div className="flex flex-col sm:flex-row gap-2 mb-6">
        <input
          type="url"
          placeholder="Enter website URL..."
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          className="flex-1 p-3 rounded border border-gray-300 focus:outline-none focus:ring focus:ring-blue-300 text-gray-900 text-sm"
        />
        <button
          onClick={fetchMetadata}
          className="px-4 py-2 rounded bg-gradient-to-r from-blue-500 to-purple-600 text-white hover:from-blue-600 hover:to-purple-700 text-sm"
          disabled={loading}
        >
          {loading ? "Loading..." : "Search"}
        </button>
      </div>

      {loading && (
        <div className="flex items-center gap-2 text-gray-600 animate-pulse mb-6 text-sm">
          <svg
            className="animate-spin h-5 w-5 text-blue-500"
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
          Fetching metadata...
        </div>
      )}

      {error && <p className="text-red-600 mb-4 text-sm">{error}</p>}
      {info && <p className="text-blue-600 mb-4 text-sm">{info}</p>}

      {currentResult && (
        <div className="bg-white shadow rounded-lg p-4 mb-8 text-sm">
          <div className="flex items-center gap-2 mb-3">
            {currentResult.favicon && (
              <img
                src={currentResult.favicon}
                onError={(e) => {
                  (e.currentTarget as HTMLImageElement).style.display = "none";
                }}
                alt="favicon"
                className="w-5 h-5"
              />
            )}
            <a
              href={currentResult.url}
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-700 underline break-all"
            >
              {currentResult.url}
            </a>
          </div>

          <div className="space-y-1 text-gray-800 break-words">
            <p>
              <strong>Title:</strong> {currentResult.title || "N/A"}
            </p>
            <p>
              <strong>Description:</strong> {currentResult.description || "N/A"}
            </p>
            <p>
              <strong>OG Title:</strong> {currentResult.ogTitle || "N/A"}
            </p>
            <p>
              <strong>OG Description:</strong>{" "}
              {currentResult.ogDescription || "N/A"}
            </p>
            {currentResult.ogImage && (
              <div className="mt-2">
                <img
                  src={currentResult.ogImage}
                  onError={(e) => {
                    const span = document.createElement("span");
                    span.textContent = "-";
                    e.currentTarget.replaceWith(span);
                  }}
                  alt="OG Image"
                  className="rounded border max-w-full h-auto max-h-40"
                />
              </div>
            )}
            <p className="text-xs text-gray-500 mt-2">
              Fetched on: {new Date(currentResult.createdAt).toLocaleString()}
            </p>
          </div>
        </div>
      )}

      <h2 className="text-lg font-semibold mb-3">Previously Searched URLs</h2>

      {loadingHistory ? (
        <div className="animate-pulse text-gray-500 text-sm">
          Loading search history...
        </div>
      ) : history.length > 0 ? (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white shadow rounded-lg text-sm">
            <thead className="bg-gray-100 text-gray-700 text-left">
              <tr>
                <th className="px-4 py-2">Favicon</th>
                <th className="px-4 py-2">URL</th>
                <th className="px-4 py-2">Title</th>
                <th className="px-4 py-2">Fetched At</th>
              </tr>
            </thead>
            <tbody>
              {paginatedHistory.map((item) => (
                <tr
                  key={item.url + item.createdAt}
                  className="border-t text-gray-800"
                >
                  <td className="px-4 py-2">
                    {item.favicon ? (
                      <img
                        src={item.favicon}
                        onError={(e) => {
                          const span = document.createElement("span");
                          span.textContent = "—";
                          e.currentTarget.replaceWith(span);
                        }}
                        alt="favicon"
                        className="w-5 h-5"
                      />
                    ) : (
                      "—"
                    )}
                  </td>
                  <td className="px-4 py-2 break-words max-w-[200px]">
                    <a
                      href={item.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-blue-600 underline"
                    >
                      {item.url}
                    </a>
                  </td>
                  <td className="px-4 py-2 break-words max-w-[180px]">
                    {item.title || "N/A"}
                  </td>
                  <td className="px-4 py-2 text-xs text-gray-500 whitespace-nowrap">
                    {new Date(item.createdAt).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex justify-between items-center px-4 py-3 bg-white border-t text-sm text-gray-700">
              <button
                onClick={handlePrevPage}
                disabled={currentPage === 1}
                className={`px-3 py-1 rounded ${
                  currentPage === 1
                    ? "bg-gray-200 cursor-not-allowed"
                    : "bg-gray-500 hover:bg-gray-600 text-white"
                }`}
              >
                Previous
              </button>
              <span>
                Page {currentPage} of {totalPages}
              </span>
              <button
                onClick={handleNextPage}
                disabled={currentPage === totalPages}
                className={`px-3 py-1 rounded ${
                  currentPage === totalPages
                    ? "bg-gray-200 cursor-not-allowed"
                    : "bg-gray-500 hover:bg-gray-600 text-white"
                }`}
              >
                Next
              </button>
            </div>
          )}
        </div>
      ) : (
        <p className="text-gray-500 text-sm">No previously searched URLs.</p>
      )}
    </div>
  );
}
