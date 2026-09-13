const clamp = (value, min, max) => Math.max(min, Math.min(max, value));

const normalizedComponent = (value, target) => {
  if (target <= 0) return 0;
  return clamp(value / target, 0, 1);
};

const roundToTwo = (value) => Math.round(value * 100) / 100;

export const calculateScoreContributions = (metrics, config) => {
  const { maxScore, earningsTarget, jobsTarget, ratingMax, activeDaysTarget, weights, exponents } = config;

  const earnings =
    Math.pow(normalizedComponent(Number(metrics.totalEarnings || 0), earningsTarget), exponents.earnings) *
    weights.earnings *
    maxScore;
  const jobs =
    Math.pow(normalizedComponent(Number(metrics.jobsCompleted || 0), jobsTarget), exponents.jobs) *
    weights.jobs *
    maxScore;
  const rating =
    Math.pow(normalizedComponent(clamp(Number(metrics.avgRating || 0), 0, ratingMax), ratingMax), exponents.rating) *
    weights.rating *
    maxScore;
  const activeDaysScore =
    Math.pow(normalizedComponent(Number(metrics.activeDays || 0), activeDaysTarget), exponents.activeDays) *
    weights.activeDays *
    maxScore;

  return {
    earnings: roundToTwo(earnings),
    jobs: roundToTwo(jobs),
    rating: roundToTwo(rating),
    activeDays: roundToTwo(activeDaysScore),
  };
};

export const calculateTotalScore = (contributions, config) => {
  const total =
    Number(contributions.earnings || 0) +
    Number(contributions.jobs || 0) +
    Number(contributions.rating || 0) +
    Number(contributions.activeDays || 0);

  return roundToTwo(clamp(total, 0, config.maxScore));
};