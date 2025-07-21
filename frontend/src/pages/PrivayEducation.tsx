import React from "react";

const cardData = [
  {
    title: "What is Fingerprinting?",
    description:
      "Websites can identify your browser and device through fingerprinting, allowing them to track you without cookies.",
    link: "https://ssd.eff.org/module/what-fingerprinting",
    source: "EFF",
  },
  {
    title: "What are Cookies and Trackers?",
    description:
      "Cookies store data in your browser. Trackers use them to follow your activity across multiple sites.",
    link: "https://www.cookieyes.com/blog/what-are-cookies/",
    source: "CookieYes",
  },
  {
    title: "Browser Fingerprinting Explained",
    description:
      "A deep dive into how fingerprinting works and how it can uniquely identify your device.",
    link: "https://restoreprivacy.com/browser-fingerprinting/",
    source: "RestorePrivacy",
  },
  {
    title: "Privacy Tools Directory",
    description:
      "A curated list of browsers, VPNs, email providers, and search engines that respect your privacy.",
    link: "https://www.privacytools.io/",
    source: "PrivacyTools.io",
  },
  {
    title: "uBlock Origin",
    description:
      "A powerful, open-source ad and tracker blocker that helps speed up browsing and protect your privacy.",
    link: "https://ublockorigin.com/",
    source: "uBlock Origin",
  },
  {
    title: "DuckDuckGo Privacy Essentials",
    description:
      "Search the web privately and block hidden trackers with this all-in-one browser extension.",
    link: "https://duckduckgo.com/app",
    source: "DuckDuckGo",
  },
  {
    title: "Mozilla VPN",
    description:
      "Encrypt your internet connection with a reliable VPN service from Mozilla.",
    link: "https://www.mozilla.org/en-US/products/vpn/",
    source: "Mozilla",
  },
  {
    title: "EFF Privacy Tools",
    description:
      "Explore privacy-focused browser extensions, secure messaging apps, and more.",
    link: "https://www.eff.org/pages/tools",
    source: "EFF",
  },
  {
    title: "Mozilla: Privacy Not Included",
    description:
      "See how smart products rank based on their respect for your personal data and security.",
    link: "https://foundation.mozilla.org/en/privacynotincluded/",
    source: "Mozilla Foundation",
  },
  {
    title: "Cover Your Tracks",
    description:
      "Test your browser to see how well it resists tracking and fingerprinting techniques.",
    link: "https://www.eff.org/pages/cover-your-tracks",
    source: "EFF",
  },
];

const PrivacyEducation: React.FC = () => {
  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto text-gray-800">
      <h1 className="text-3xl font-bold mb-8 text-gradient bg-gradient-to-r from-blue-500 to-purple-600 bg-clip-text text-transparent">
        Privacy Education
      </h1>

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {cardData.map((card, idx) => (
          <div
            key={idx}
            className="bg-white border border-gray-200 rounded-xl shadow-sm p-6 hover:shadow-md transition duration-300 flex flex-col justify-between"
          >
            <div>
              <h2 className="text-lg font-semibold mb-2">{card.title}</h2>
              <p className="text-sm text-gray-700 mb-4">{card.description}</p>
            </div>
            <a
              href={card.link}
              target="_blank"
              rel="noopener noreferrer"
              className="text-sm text-blue-600 hover:underline font-medium"
            >
              Learn more at {card.source}
            </a>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PrivacyEducation;
