import React from 'react';
import { Truck, Headset, DollarSign, ShieldCheck, RefreshCcw, Users, Sparkles, ThumbsUp, Shield } from 'lucide-react';
import { motion } from "framer-motion";
const fadeInUp = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.6 } },
};

const services = [
  {
    title: 'Fast Delivery',
    description: 'Get your items delivered quickly and safely.',
    icon: <Truck className="h-8 w-8 text-blue-600" />,
  },
  {
    title: '24/7 Support',
    description: 'Always here to help, any time, any day.',
    icon: <Headset className="h-8 w-8 text-blue-600" />,
  },
  {
    title: 'Affordable Pricing',
    description: 'Top-notch service at a reasonable price.',
    icon: <DollarSign className="h-8 w-8 text-blue-600" />,
  },
  {
    title: 'Trusted by Thousands',
    description: 'Used by thousands of happy customers.',
    icon: <Users className="h-8 w-8 text-blue-600" />,
  },
  {
    title: 'Secure Payments',
    description: 'Your payment info is protected with us.',
    icon: <ShieldCheck className="h-8 w-8 text-blue-600" />,
  },
  {
    title: 'Easy Returns',
    description: 'No-hassle return policy for peace of mind.',
    icon: <RefreshCcw className="h-8 w-8 text-blue-600" />,
  },
];

const reviews = [
  {
    name: 'Alice',
    comment: 'Amazing experience! Super fast and friendly support.',
  },
  {
    name: 'Bob',
    comment: 'Affordable and reliable. Will use again.',
  },
  {
    name: 'Carol',
    comment: 'Everything went smoothly from start to finish.',
  },
];

const Home: React.FC = () => {
  return (
    <div className="bg-blue-50 text-gray-800">
      {/* Hero Section */}
      <motion.section
        initial="hidden"
        whileInView="visible"
        viewport={{ once: true }}
        variants={fadeInUp}
        className="relative bg-gray-900 text-white py-28 px-6 overflow-hidden"
      >
        <div className="absolute inset-0 bg-gradient-to-br from-blue-700/30 to-blue-400/10 z-0" />
        <div className="relative z-10 max-w-4xl mx-auto text-center">
          <h1 className="text-4xl md:text-5xl font-extrabold mb-6 drop-shadow-lg">
            Welcome to Our Service
          </h1>
          <p className="text-lg md:text-xl max-w-2xl mx-auto text-gray-300">
            We provide fast, secure, and reliable solutions to simplify your life and business.
          </p>
        </div>
        <div className="absolute top-0 left-0 w-48 h-48 bg-blue-600 rounded-full opacity-20 blur-3xl animate-pulse -z-10" />
        <div className="absolute bottom-0 right-0 w-64 h-64 bg-blue-400 rounded-full opacity-10 blur-2xl -z-10" />
      </motion.section>

      {/* About Us Section with Image */}
      <section className="py-20 px-6 bg-white">
        <div className="max-w-6xl mx-auto grid md:grid-cols-2 gap-10 items-center">
          <img
            src="/home_image.jpg"
            alt="Our Team"
            className="rounded-xl shadow-md w-full object-cover h-80"
          />
          <div>
            <h2 className="text-3xl font-bold mb-6">About Us</h2>
            <p className="mb-4 text-lg text-gray-700">
              <strong>Our Mission:</strong> To empower individuals and businesses with seamless,
              secure, and innovative digital services that improve everyday life.
            </p>
            <p className="text-lg text-gray-700">
              <strong>Our Vision:</strong> To become a globally trusted platform known for
              simplicity, speed, and reliability.
            </p>
          </div>
        </div>
      </section>

      {/* Discover Section */}
      <section className="py-20 px-6 bg-blue-100">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-3xl font-bold mb-4">Discover the Future of Service</h2>
          <p className="text-lg text-gray-700">
            We’re redefining what it means to deliver great experiences — with smart technology,
            customer-first policies, and a passion for simplicity.
          </p>
        </div>
      </section>

      {/* Services Section */}
      <section className="py-20 px-6">
        <h2 className="text-3xl font-bold text-center mb-12">What We Offer</h2>
        <motion.div
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true }}
          variants={{
            hidden: {},
            visible: {
              transition: {
                staggerChildren: 0.15,
              },
            },
          }}
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8 max-w-6xl mx-auto"
        >
          {services.map((service, i) => (
            <motion.div
              key={i}
              variants={fadeInUp}
              className="bg-white p-6 rounded-2xl shadow transition-transform duration-300 hover:shadow-xl hover:-translate-y-1 hover:scale-[1.03] cursor-pointer"
            >
              <div className="mb-4">{service.icon}</div>
              <h3 className="text-xl font-semibold mb-2">{service.title}</h3>
              <p className="text-sm text-gray-600">{service.description}</p>
            </motion.div>
          ))}
        </motion.div>

      </section>

      {/* Why Us Section */}
      <section className="py-20 px-6 bg-white">
        <h2 className="text-3xl font-bold text-center mb-12">Why Choose Us</h2>
        <div className="max-w-5xl mx-auto grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
          <div className="bg-blue-100 p-6 rounded-xl shadow hover:shadow-md transition-transform duration-300 hover:-translate-y-1">
            <Sparkles className="w-8 h-8 mx-auto text-blue-600 mb-4" />
            <h3 className="font-semibold text-lg mb-2">Innovative Solutions</h3>
            <p className="text-sm text-gray-700">We stay ahead by continuously improving and innovating our platform.</p>
          </div>
          <div className="bg-blue-100 p-6 rounded-xl shadow hover:shadow-md transition-transform duration-300 hover:-translate-y-1">
            <ThumbsUp className="w-8 h-8 mx-auto text-blue-600 mb-4" />
            <h3 className="font-semibold text-lg mb-2">Top Rated Support</h3>
            <p className="text-sm text-gray-700">Our users love our responsive and knowledgeable support team.</p>
          </div>
          <div className="bg-blue-100 p-6 rounded-xl shadow hover:shadow-md transition-transform duration-300 hover:-translate-y-1">
            <Shield className="w-8 h-8 mx-auto text-blue-600 mb-4" />
            <h3 className="font-semibold text-lg mb-2">Rock-Solid Security</h3>
            <p className="text-sm text-gray-700">Your data and transactions are protected with industry-grade security.</p>
          </div>
        </div>
      </section>

      {/* Reviews Section with Image */}
      <section className="py-20 px-6 bg-blue-50">
        <div className="max-w-6xl mx-auto grid md:grid-cols-2 gap-10 items-center">
          <img
            src="/home_image_2.jpg"
            alt="Happy Customers"
            className="rounded-xl shadow-md w-full object-cover h-80"
          />
          <div>
            <h2 className="text-3xl font-bold mb-6 text-center md:text-left">What Our Customers Say</h2>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              {reviews.map((review, i) => (
                <div
                  key={i}
                  className="bg-gray-100 p-4 rounded-xl shadow-sm transition-transform duration-300 hover:shadow-md hover:-translate-y-1 hover:scale-[1.02] cursor-pointer"
                >
                  <p className="italic text-gray-800 mb-3">"{review.comment}"</p>
                  <p className="text-right font-medium text-gray-600">— {review.name}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-gray-900 text-white py-20 px-6 text-center">
        <h2 className="text-3xl md:text-4xl font-semibold mb-4">
          Join Thousands of Happy Customers
        </h2>
        <p className="text-lg mb-8 max-w-xl mx-auto">
          Start using our platform today and enjoy a seamless, secure, and smart experience.
        </p>
        <button className="bg-blue-600 hover:bg-blue-500 text-white font-bold px-8 py-3 rounded-full shadow-md transition">
          Get Started
        </button>
      </section>
    </div>
  );
};

export default Home;
