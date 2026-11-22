const MAX_SCORE = 100;
const EARNINGS_TARGET = 5000;
const JOBS_TARGET = 100;
const RATING_MAX = 5.0;
const ACTIVE_DAYS_TARGET = 30;

const WEIGHT_EARNINGS = 0.35;
const WEIGHT_JOBS = 0.25;
const WEIGHT_RATING = 0.30;
const WEIGHT_ACTIVE_DAYS = 0.10;

const EXP_EARNINGS = 0.70;
const EXP_JOBS = 0.65;
const EXP_RATING = 1.40;
const EXP_ACTIVE_DAYS = 1.00;

const clamp = (value, min, max) => Math.max(min, Math.min(max, value));

const normalizedComponent = (value, target) => {
  if (target <= 0) return 0;
  return clamp(value / target, 0, 1);
};

const roundToTwo = (value) => Math.round(value * 100) / 100;

export const calculateScoreContributions = ({ totalEarnings, jobsCompleted, avgRating, activeDays }) => {
  const earnings =
    Math.pow(normalizedComponent(Number(totalEarnings || 0), EARNINGS_TARGET), EXP_EARNINGS) *
    WEIGHT_EARNINGS *
    MAX_SCORE;
  const jobs =
    Math.pow(normalizedComponent(Number(jobsCompleted || 0), JOBS_TARGET), EXP_JOBS) *
    WEIGHT_JOBS *
    MAX_SCORE;
  const rating =
    Math.pow(normalizedComponent(clamp(Number(avgRating || 0), 0, RATING_MAX), RATING_MAX), EXP_RATING) *
    WEIGHT_RATING *
    MAX_SCORE;
  const consistency =
    Math.pow(normalizedComponent(Number(activeDays || 0), ACTIVE_DAYS_TARGET), EXP_ACTIVE_DAYS) *
    WEIGHT_ACTIVE_DAYS *
    MAX_SCORE;

  return {
    earnings: roundToTwo(earnings),
    jobs: roundToTwo(jobs),
    rating: roundToTwo(rating),
    activeDays: roundToTwo(consistency),
    // Backward compatibility aliases
    consistency: roundToTwo(consistency),
  };
};

export const calculateTotalScore = (contributions) => {
  const total =
    Number(contributions.earnings || 0) +
    Number(contributions.jobs || 0) +
    Number(contributions.rating || 0) +
    Number(contributions.activeDays || contributions.consistency || 0);

  return roundToTwo(clamp(total, 0, MAX_SCORE));
};

export const getScoreBand = (score, bands) => {
  const excellent = bands?.Excellent ?? 90.0;
  const strong = bands?.Strong ?? 75.0;
  const fair = bands?.Fair ?? 50.0;

  if (score >= excellent) return "Excellent";
  if (score >= strong) return "Strong";
  if (score >= fair) return "Fair";
  return "Needs Improvement";
};

export const getRiskClassification = (score, bands) => {
  const excellent = bands?.Excellent ?? 90.0;
  const strong = bands?.Strong ?? 75.0;
  const fair = bands?.Fair ?? 50.0;

  if (score >= excellent) {
    return { band: "Excellent", label: "Excellent", risk: "Very Low Risk", color: "emerald" };
  }
  if (score >= strong) {
    return { band: "Strong", label: "Strong", risk: "Low Risk", color: "blue" };
  }
  if (score >= fair) {
    return { band: "Fair", label: "Fair", risk: "Moderate Risk", color: "amber" };
  }
  return { band: "Needs Improvement", label: "Needs Improvement", risk: "High Risk", color: "rose" };
};

export const SCORE_CONFIG = {
  maxScore: MAX_SCORE,
  earningsTarget: EARNINGS_TARGET,
  jobsTarget: JOBS_TARGET,
  activeDaysTarget: ACTIVE_DAYS_TARGET,
  ratingMax: RATING_MAX,
  bands: {
    Excellent: 90.0,
    Strong: 75.0,
    Fair: 50.0,
    "Needs Improvement": 0.0,
  },
  weights: {
    earnings: WEIGHT_EARNINGS,
    jobs: WEIGHT_JOBS,
    rating: WEIGHT_RATING,
    activeDays: WEIGHT_ACTIVE_DAYS,
  },
  exponents: {
    earnings: EXP_EARNINGS,
    jobs: EXP_JOBS,
    rating: EXP_RATING,
    activeDays: EXP_ACTIVE_DAYS,
  },
};
