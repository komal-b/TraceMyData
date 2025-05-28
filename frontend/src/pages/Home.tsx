import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import privacy from '../assets/privacy.png';
import chart from '../assets/chart.png';
import monitor from '../assets/monitor.png';
export default function Home() {
  return (
    <div className="min-h-screen w-full overflow-x-hidden bg-white text-gray-900 py-12 px-4">
      <div className="max-w-screen-xl w-full mx-auto px-4 sm:px-6 md:px-8 text-center">

        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <h1 className="text-[36px] md:text-[48px] font-bold mb-6 bg-clip-text text-transparent bg-gradient-to-r from-blue-500 to-purple-500">
            TraceMyData
          </h1>
        </motion.div>

        {/* Tagline */}
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.3, duration: 0.8 }}
          className="text-[18px] md:text-[24px] mb-12 text-gray-700 max-w-[700px] mx-auto leading-relaxed"
        >
          Your <span className="font-semibold text-blue-600">privacy companion</span> for the digital age. 
          Discover, visualize, and control how your data moves across the web.
        </motion.p>

        {/* Feature highlights */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-[24px] w-full mb-16">
          {[
            {
              icon: monitor, // Replace with your actual icon path
              title: 'Data Tracking',
              description: 'See exactly what websites collect about you'
            },
            {
              icon: chart,
              title: 'Visual Analytics',
              description: 'Understand your data flow with clear visuals'
            },
            {
              icon: privacy,
              title: 'Privacy Control',
              description: 'Take action to protect your information'
            }
          ].map((feature, index) => (
            <motion.div
              key={feature.title}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5 + index * 0.1, duration: 0.5 }}
              className="bg-white p-[20px] rounded-xl shadow-md border border-gray-200 hover:shadow-lg transition-all"
            >
              <img
                src={feature.icon}
                alt={feature.title}
                className="w-12 h-12 mx-auto mb-4"
              />
              <h3 className="text-[20px] font-semibold mb-2">{feature.title}</h3>
              <p className="text-[14px] text-gray-600">{feature.description}</p>
            </motion.div>
          ))}
        </div>

        {/* CTA Buttons */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.8, duration: 0.5 }}
          className="flex flex-col md:flex-row justify-center gap-4 mb-12"
        >
          <Link 
            to="/register" 
            className="px-6 py-4 bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 rounded-lg text-white font-semibold text-[16px] transition-all shadow-md hover:shadow-blue-900/30"
          >
            Get Started - It's Free
          </Link>
          <Link 
            to="/login" 
            className="px-6 py-4 border border-gray-300 hover:bg-gray-100 rounded-lg text-gray-800 font-semibold text-[16px] transition-all"
          >
            Existing User? Login
          </Link>
        </motion.div>

        {/* Footer */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1, duration: 0.5 }}
          className="text-gray-500 text-[12px]"
        >
          <p>Trusted by privacy-conscious users worldwide</p>
          <div className="flex flex-wrap justify-center gap-4 mt-3 opacity-80">
            {['GDPR', 'CCPA', 'Encrypted', 'Open Source'].map((item) => (
              <span key={item} className="flex items-center">
                <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
                {item}
              </span>
            ))}
          </div>
        </motion.div>

      </div>
    </div>
  );
}
