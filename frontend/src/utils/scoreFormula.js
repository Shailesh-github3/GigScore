const MAX_SCORE = 100;
const EARNINGS_TARGET = 5000;
const ACTIVE_DAYS_TARGET = 30;
const RATING_MAX = 5;
const PLATFORMS_TARGET = 3;

const WEIGHT_EARNINGS = 0.40;
const WEIGHT_CONSISTENCY = 0.30;
const WEIGHT_RATING = 0.20;
const WEIGHT_PLATFORMS = 0.10;

const EXP_EARNINGS = 0.7;
const EXP_CONSISTENCY = 1.0;
const EXP_RATING = 1.4;
const EXP_PLATFORMS = 1.0;

const clamp = (value, min, max) => Math.max(min, Math.min(max, value));

const normalizedComponent = (value, target) => {
  if (target <= 0) return 0;
  return clamp(value / target, 0, 1);
};

const roundToTwo = (value) => Math.round(value * 100) / 100;

export const calculateScoreContributions = ({ totalEarnings, activeDays, avgRating, platformsUsed }) => {
  const earnings =
    Math.pow(normalizedComponent(Number(totalEarnings || 0), EARNINGS_TARGET), EXP_EARNINGS) *
    WEIGHT_EARNINGS *
    MAX_SCORE;
  const consistency =
    Math.pow(normalizedComponent(Number(activeDays || 0), ACTIVE_DAYS_TARGET), EXP_CONSISTENCY) *
    WEIGHT_CONSISTENCY *
    MAX_SCORE;
  const rating =
    Math.pow(normalizedComponent(clamp(Number(avgRating || 0), 0, RATING_MAX), RATING_MAX), EXP_RATING) *
    WEIGHT_RATING *
    MAX_SCORE;
  const platforms =
    Math.pow(normalizedComponent(Number(platformsUsed || 0), PLATFORMS_TARGET), EXP_PLATFORMS) *
    WEIGHT_PLATFORMS *
    MAX_SCORE;

  return {
    earnings: roundToTwo(earnings),
    consistency: roundToTwo(consistency),
    rating: roundToTwo(rating),
    platforms: roundToTwo(platforms),
  };
};

export const calculateTotalScore = (contributions) => {
  const total =
    Number(contributions.earnings || 0) +
    Number(contributions.consistency || 0) +
    Number(contributions.rating || 0) +
    Number(contributions.platforms || 0);

  return roundToTwo(clamp(total, 0, MAX_SCORE));
};

export const SCORE_CONFIG = {
  maxScore: MAX_SCORE,
  earningsTarget: EARNINGS_TARGET,
  activeDaysTarget: ACTIVE_DAYS_TARGET,
  ratingMax: RATING_MAX,
  platformsTarget: PLATFORMS_TARGET,
  weights: {
    earnings: WEIGHT_EARNINGS,
    consistency: WEIGHT_CONSISTENCY,
    rating: WEIGHT_RATING,
    platforms: WEIGHT_PLATFORMS,
  },
  exponents: {
    earnings: EXP_EARNINGS,
    consistency: EXP_CONSISTENCY,
    rating: EXP_RATING,
    platforms: EXP_PLATFORMS,
  },
};
