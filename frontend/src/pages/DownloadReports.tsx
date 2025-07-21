import React from "react";

const DownloadReports: React.FC = () => {
  const handleDownload = async (format: "csv") => {
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

    try {
      const response = await fetch(`http://localhost:8080/api/reports?format=${format}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${getToken()}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to download file.");
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `scan_results.${format}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error("Download error:", error);
      alert("Failed to download the report.");
    }
  };

  return (
    <div className="flex space-x-4 items-center justify-center">
      <button
        onClick={() => handleDownload("csv")}
        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
      >
        Download CSV
      </button>
    </div>
  );
};

export default DownloadReports;
