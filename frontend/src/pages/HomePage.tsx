import React, { useEffect, useRef, useState } from "react";
import {
  motion,
  useReducedMotion,
  useScroll,
  useSpring,
  useTransform,
  useInView,
} from "framer-motion";
import {
  Truck,
  Headset,
  DollarSign,
  ShieldCheck,
  RefreshCcw,
  Users,
  Sparkles,
  ThumbsUp,
  Shield,
} from "lucide-react";

// ---- Animation helpers (respect reduced motion) ----
const useAnims = () => {
  const prefersReducedMotion = useReducedMotion();
  const fadeInUp = prefersReducedMotion
    ? {
        hidden: { opacity: 0 },
        visible: { opacity: 1, transition: { duration: 0.4 } },
      }
    : {
        hidden: { opacity: 0, y: 24 },
        visible: { opacity: 1, y: 0, transition: { duration: 0.6 } },
      };
  const stagger = prefersReducedMotion
    ? { hidden: {}, visible: { transition: { staggerChildren: 0.05 } } }
    : { hidden: {}, visible: { transition: { staggerChildren: 0.12 } } };
  return { fadeInUp, stagger };
};

// ---- Data ----
const services = [
  {
    title: "Fast Delivery",
    description: "Get your items delivered quickly and safely.",
    icon: Truck,
  },
  {
    title: "24/7 Support",
    description: "Always here to help, any time, any day.",
    icon: Headset,
  },
  {
    title: "Affordable Pricing",
    description: "Top‑notch service at a reasonable price.",
    icon: DollarSign,
  },
  {
    title: "Trusted by Thousands",
    description: "Used by thousands of happy customers.",
    icon: Users,
  },
  {
    title: "Secure Payments",
    description: "Your payment info is protected with us.",
    icon: ShieldCheck,
  },
  {
    title: "Easy Returns",
    description: "No‑hassle return policy for peace of mind.",
    icon: RefreshCcw,
  },
];

const reviews = [
  {
    name: "Alice",
    comment: "Amazing experience! Super fast and friendly support.",
  },
  { name: "Bob", comment: "Affordable and reliable. Will use again." },
  { name: "Carol", comment: "Everything went smoothly from start to finish." },
  {
    name: "Diego",
    comment: "Great UX and top‑tier security. Highly recommend.",
  },
];

const stats = [
  { label: "Deliveries", value: 1200000, display: "1.2M+" },
  { label: "Avg. Response", value: 2, display: "< 2 min" },
  { label: "Customer Satisfaction", value: 99.3, display: "99.3%" },
  { label: "Countries", value: 40, display: "40+" },
];

// ---- Small UI components ----
function SectionTitle({ kicker, title, subtitle, align = "center" }) {
  return (
    <div
      className={`max-w-3xl mx-auto ${align === "left" ? "text-left" : "text-center"}`}
    >
      {kicker ? (
        <p className="text-sm uppercase tracking-widest text-blue-600 font-semibold mb-2">
          {kicker}
        </p>
      ) : null}
      <h2 className="text-3xl md:text-4xl font-extrabold text-gray-900 mb-3">
        {title}
      </h2>
      {subtitle ? <p className="text-lg text-gray-700">{subtitle}</p> : null}
    </div>
  );
}

function FeatureCard({ Icon, title, description }) {
  // subtle tilt-on-scroll using motion values
  const ref = useRef(null);
  const isInView = useInView(ref, { once: false, margin: "-10% 0px -10% 0px" });
  const { scrollY } = useScroll();
  const y = useTransform(scrollY, [0, 600], [0, -6]);

  return (
    <motion.div
      ref={ref}
      style={{ y }}
      whileHover={{ scale: 1.02, translateY: -4 }}
      className="bg-white/90 backdrop-blur p-6 rounded-2xl shadow transition-transform duration-300 border border-blue-50"
      animate={{
        boxShadow: isInView
          ? "0 10px 24px rgba(30,64,175,0.08)"
          : "0 2px 8px rgba(0,0,0,0.04)",
      }}
      transition={{ type: "spring", stiffness: 120, damping: 18 }}
    >
      <div className="inline-flex items-center justify-center rounded-xl bg-blue-50 p-3 mb-4">
        <Icon aria-hidden className="h-6 w-6 text-blue-600" />
      </div>
      <h3 className="text-lg font-semibold text-gray-900 mb-1">{title}</h3>
      <p className="text-sm text-gray-600">{description}</p>
    </motion.div>
  );
}

function CountUp({ to, prefix = "", suffix = "" }) {
  const ref = useRef(null);
  const inView = useInView(ref, { once: true, margin: "-10% 0px -10% 0px" });
  const [val, setVal] = useState(0);

  useEffect(() => {
    if (!inView) return;
    const duration = 900; // ms
    const start = performance.now();
    const from = 0;
    const animate = (t) => {
      const p = Math.min(1, (t - start) / duration);
      const eased = 1 - Math.pow(1 - p, 3); // easeOutCubic
      setVal(Math.floor(from + (to - from) * eased));
      if (p < 1) requestAnimationFrame(animate);
    };
    const r = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(r);
  }, [inView, to]);

  return (
    <span ref={ref}>
      {prefix}
      {val.toLocaleString()}
      {suffix}
    </span>
  );
}

function Stat({ value, label, display }) {
  const isPercent = /%$/.test(String(display));
  return (
    <div className="text-center">
      <div className="text-3xl md:text-4xl font-extrabold text-gray-900">
        {typeof value === "number" && /[+%]/.test(display) ? (
          <>
            <CountUp to={Math.round(value)} />
            {display.includes("+") ? "+" : ""}
            {isPercent ? "%" : ""}
          </>
        ) : (
          display
        )}
      </div>
      <div className="text-sm text-gray-600 mt-1">{label}</div>
    </div>
  );
}

function Testimonial({ name, comment }) {
  return (
    <figure className="bg-white p-5 rounded-2xl shadow-sm border border-blue-50">
      <blockquote className="text-gray-800 italic">“{comment}”</blockquote>
      <figcaption className="mt-3 text-right text-sm font-medium text-gray-600">
        — {name}
      </figcaption>
    </figure>
  );
}

function ScrollProgressBar() {
  const { scrollYProgress } = useScroll();
  const scaleX = useSpring(scrollYProgress, {
    stiffness: 120,
    damping: 24,
    mass: 0.2,
  });
  return (
    <motion.div
      aria-hidden
      style={{ scaleX }}
      className="fixed left-0 right-0 top-0 h-1 origin-left bg-blue-600 z-50"
    />
  );
}

function ParallaxBlobs() {
  const ref = useRef(null);
  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start start", "end start"],
  });
  const y1 = useTransform(scrollYProgress, [0, 1], [0, 80]);
  const y2 = useTransform(scrollYProgress, [0, 1], [0, -60]);
  return (
    <div ref={ref} className="absolute inset-0 -z-10">
      <motion.div
        style={{ y: y1 }}
        className="absolute top-[-6rem] left-[-6rem] w-72 h-72 rounded-full bg-blue-600 opacity-20 blur-3xl"
      />
      <motion.div
        style={{ y: y2 }}
        className="absolute bottom-[-8rem] right-[-6rem] w-96 h-96 rounded-full bg-blue-400 opacity-10 blur-2xl"
      />
      <div className="absolute inset-0 bg-gradient-to-br from-blue-700/30 to-blue-400/10" />
    </div>
  );
}

// ---- Page ----
export default function Home() {
  const { fadeInUp, stagger } = useAnims();
  const heroRef = useRef(null);
  const { scrollY } = useScroll({
    target: heroRef,
    offset: ["start start", "end start"],
  });
  const heroTranslateY = useTransform(scrollY, [0, 300], [0, -30]);
  const heroOpacity = useTransform(scrollY, [0, 300], [1, 0.85]);

  return (
    <main className="bg-blue-50 text-gray-800">
      {/* HERO */}
      <section
        ref={heroRef}
        aria-label="Hero"
        className="relative isolate overflow-hidden bg-gray-900 text-white"
      >
        <ParallaxBlobs />
        <motion.div
          style={{ y: heroTranslateY, opacity: heroOpacity }}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.4 }}
          variants={fadeInUp}
          className="mx-auto max-w-6xl px-6 py-28 text-center"
        >
          <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight drop-shadow-sm">
            Welcome to Our Service
          </h1>
          <p className="mt-5 text-lg md:text-xl max-w-2xl mx-auto text-gray-300">
            We provide fast, secure, and reliable solutions to simplify your
            life and business.
          </p>
          <motion.div
            className="mt-8 flex items-center justify-center gap-3"
            initial={{ opacity: 0, y: 12 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            <a
              href="#get-started"
              className="inline-flex items-center justify-center rounded-full bg-blue-600 px-6 py-3 font-semibold text-white shadow-md hover:bg-blue-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/70"
            >
              Get Started
            </a>
            <a
              href="#how-it-works"
              className="inline-flex items-center justify-center rounded-full border border-white/30 px-6 py-3 font-semibold text-white/90 hover:bg-white/10 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/70"
            >
              How it Works
            </a>
          </motion.div>
          <motion.div
            className="mt-10 opacity-80"
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.8 }}
          >
            <p className="text-xs uppercase tracking-widest text-gray-300">
              Trusted by teams worldwide
            </p>
            <div className="mt-3 grid grid-cols-2 md:grid-cols-4 gap-6 opacity-70">
              <div className="h-8 bg-white/10 rounded" />
              <div className="h-8 bg-white/10 rounded" />
              <div className="h-8 bg-white/10 rounded" />
              <div className="h-8 bg-white/10 rounded" />
            </div>
          </motion.div>
        </motion.div>
      </section>

      {/* ABOUT + IMAGE */}
      <section aria-label="About us" className="py-20 px-6 bg-white">
        <div className="max-w-6xl mx-auto grid md:grid-cols-2 gap-10 items-center">
          <motion.img
            src="/home_image.jpg"
            alt="Team collaborating in a modern workspace"
            loading="lazy"
            className="rounded-2xl shadow-md w-full object-cover h-80"
            initial={{ opacity: 0, scale: 0.98 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true, amount: 0.4 }}
            transition={{ duration: 0.6 }}
          />
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true, amount: 0.4 }}
            variants={fadeInUp}
          >
            <SectionTitle
              align="left"
              kicker="About us"
              title="Built for simplicity, speed & security"
              subtitle="Our mission is to empower individuals and businesses with seamless, secure, and innovative digital services that improve everyday life."
            />
            <ul className="mt-6 space-y-3 text-gray-700">
              <li className="flex gap-3">
                <Shield className="w-5 h-5 text-blue-600 mt-1" />
                <span>
                  <strong>Vision:</strong> To be a globally trusted platform
                  known for reliability and ease.
                </span>
              </li>
              <li className="flex gap-3">
                <ThumbsUp className="w-5 h-5 text-blue-600 mt-1" />
                <span>
                  <strong>Principle:</strong> Customer‑first policies and
                  transparent pricing.
                </span>
              </li>
            </ul>
          </motion.div>
        </div>
      </section>

      {/* DISCOVER */}
      <section aria-label="Discover" className="py-20 px-6 bg-blue-100">
        <SectionTitle
          title="Discover the Future of Service"
          subtitle="We’re redefining great experiences with smart technology, customer‑first policies, and a passion for simplicity."
        />
        <motion.div
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, amount: 0.3 }}
          variants={stagger}
          className="mt-12 grid grid-cols-2 md:grid-cols-4 gap-6 max-w-5xl mx-auto"
        >
          {stats.map((s, i) => (
            <motion.div key={i} variants={fadeInUp}>
              <Stat value={s.value} label={s.label} display={s.display} />
            </motion.div>
          ))}
        </motion.div>
      </section>

      {/* SERVICES */}
      <section aria-label="Services" className="py-20 px-6">
        <SectionTitle
          title="What We Offer"
          subtitle="Everything you need to move fast and stay secure."
        />
        <motion.div
          initial="hidden"
          whileInView="visible"
          viewport={{ once: false, amount: 0.2 }}
          variants={stagger}
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mt-12 max-w-6xl mx-auto"
        >
          {services.map((s, i) => (
            <motion.div key={i} variants={fadeInUp}>
              <FeatureCard
                Icon={s.icon}
                title={s.title}
                description={s.description}
              />
            </motion.div>
          ))}
        </motion.div>
      </section>

      {/* WHY US */}
      <section aria-label="Why choose us" className="py-20 px-6 bg-white">
        <SectionTitle title="Why Choose Us" />
        <div className="max-w-5xl mx-auto grid grid-cols-1 sm:grid-cols-3 gap-6 text-center mt-10">
          {[
            {
              Icon: Sparkles,
              t: "Innovative Solutions",
              d: "We stay ahead by continuously improving and innovating our platform.",
            },
            {
              Icon: ThumbsUp,
              t: "Top Rated Support",
              d: "Our users love our responsive and knowledgeable support team.",
            },
            {
              Icon: Shield,
              t: "Rock‑Solid Security",
              d: "Your data and transactions are protected with industry‑grade security.",
            },
          ].map((item, idx) => (
            <motion.div
              key={idx}
              initial={{ opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.4 }}
              transition={{ duration: 0.5, delay: idx * 0.05 }}
              className="bg-blue-100 p-6 rounded-2xl shadow hover:shadow-md transition-transform duration-300 hover:-translate-y-1"
            >
              <item.Icon className="w-8 h-8 mx-auto text-blue-600 mb-3" />
              <h3 className="font-semibold text-lg mb-1">{item.t}</h3>
              <p className="text-sm text-gray-700">{item.d}</p>
            </motion.div>
          ))}
        </div>
      </section>

      {/* HOW IT WORKS */}
      <section
        id="how-it-works"
        aria-label="How it works"
        className="py-20 px-6 bg-blue-50/60"
      >
        <SectionTitle
          title="How it Works"
          subtitle="Three easy steps to get value fast."
        />
        <ol className="mt-10 max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            "Sign up in minutes",
            "Connect your tools securely",
            "Track results in real time",
          ].map((step, i) => (
            <motion.li
              key={i}
              initial={{ opacity: 0, y: 12 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.5 }}
              transition={{ duration: 0.45 }}
              className="bg-white rounded-2xl shadow p-6 border border-blue-50"
            >
              <div className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-blue-600 text-white font-bold">
                {i + 1}
              </div>
              <p className="mt-3 text-gray-800 font-medium">{step}</p>
            </motion.li>
          ))}
        </ol>
      </section>

      {/* REVIEWS + IMAGE */}
      <section aria-label="Customer reviews" className="py-20 px-6 bg-blue-50">
        <div className="max-w-6xl mx-auto grid md:grid-cols-2 gap-10 items-center">
          <motion.img
            src="/home_image_2.jpg"
            alt="Happy customers using the product"
            loading="lazy"
            className="rounded-2xl shadow-md w-full object-cover h-80"
            initial={{ opacity: 0, x: -12 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true, amount: 0.4 }}
            transition={{ duration: 0.6 }}
          />
          <div>
            <SectionTitle align="left" title="What Our Customers Say" />
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-6">
              {reviews.map((r, i) => (
                <motion.div
                  key={i}
                  initial={{ opacity: 0, y: 12 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.4 }}
                >
                  <Testimonial name={r.name} comment={r.comment} />
                </motion.div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* FAQ */}
      <section aria-label="FAQ" className="py-20 px-6 bg-white">
        <SectionTitle
          title="Frequently Asked Questions"
          subtitle="Quick answers to common questions."
        />
        <div className="max-w-4xl mx-auto mt-8 space-y-4">
          {[
            {
              q: "Can I cancel anytime?",
              a: "Yes, you can cancel your plan at any time from your account settings.",
            },
            {
              q: "Do you offer refunds?",
              a: "We have a 30‑day money‑back guarantee if you’re not satisfied.",
            },
            {
              q: "Is my data secure?",
              a: "We use modern encryption and follow best practices to keep your data safe.",
            },
          ].map((item, i) => (
            <motion.details
              key={i}
              className="group bg-blue-50/60 rounded-2xl p-5 border border-blue-100"
              initial={{ opacity: 0, scale: 0.98 }}
              whileInView={{ opacity: 1, scale: 1 }}
              viewport={{ once: true }}
              transition={{ duration: 0.4 }}
            >
              <summary className="cursor-pointer list-none select-none flex items-center justify-between text-left">
                <span className="font-semibold text-gray-900">{item.q}</span>
                <span className="ml-4 text-blue-600 group-open:rotate-180 transition-transform">
                  ▾
                </span>
              </summary>
              <p className="mt-3 text-gray-700">{item.a}</p>
            </motion.details>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section
        id="get-started"
        aria-label="Call to action"
        className="bg-gray-900 text-white py-20 px-6 text-center"
      >
        <motion.h2
          className="text-3xl md:text-4xl font-semibold"
          initial={{ opacity: 0, y: 10 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
        >
          Join Thousands of Happy Customers
        </motion.h2>
        <motion.p
          className="text-lg mt-3 max-w-xl mx-auto text-white/90"
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          transition={{ delay: 0.1 }}
        >
          Start using our platform today and enjoy a seamless, secure, and smart
          experience.
        </motion.p>
        <motion.div
          className="mt-8 inline-flex items-center gap-3"
          initial={{ opacity: 0, y: 8 }}
          whileInView={{ opacity: 1, y: 0 }}
        >
          <a
            href="#"
            className="rounded-full bg-blue-600 hover:bg-blue-500 text-white font-bold px-8 py-3 shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-white/70"
          >
            Get Started
          </a>
          <a
            href="#"
            className="rounded-full border border-white/30 px-8 py-3 font-semibold text-white/90 hover:bg-white/10 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/70"
          >
            Contact Sales
          </a>
        </motion.div>
        <p className="mt-6 text-sm text-white/70">
          Free 14‑day trial • No credit card required
        </p>
      </section>
    </main>
  );
}
