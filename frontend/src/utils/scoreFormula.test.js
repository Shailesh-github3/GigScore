import { describe, expect, it } from "vitest";
import { calculateScoreContributions, calculateTotalScore } from "./scoreFormula";

const config = {
  maxScore: 100,
  earningsTarget: 5000,
  jobsTarget: 100,
  ratingMax: 5,
  activeDaysTarget: 30,
  weights: { earnings: 0.35, jobs: 0.25, rating: 0.3, activeDays: 0.1 },
  exponents: { earnings: 0.7, jobs: 0.65, rating: 1.4, activeDays: 1.0 },
};

describe("scoreFormula", () => {
  it("returns zeros for empty metrics", () => {
    const contributions = calculateScoreContributions(
      { totalEarnings: 0, jobsCompleted: 0, avgRating: 0, activeDays: 0 },
      config,
    );

    expect(contributions).toEqual({ earnings: 0, jobs: 0, rating: 0, activeDays: 0 });
    expect(calculateTotalScore(contributions, config)).toBe(0);
  });

  it("caps every component at its weight for maxed metrics", () => {
    const contributions = calculateScoreContributions(
      { totalEarnings: 20000, jobsCompleted: 500, avgRating: 5, activeDays: 100 },
      config,
    );

    expect(contributions).toEqual({ earnings: 35, jobs: 25, rating: 30, activeDays: 10 });
    expect(calculateTotalScore(contributions, config)).toBe(100);
  });

  it("clamps ratings above the max", () => {
    const over = calculateScoreContributions(
      { totalEarnings: 2500, jobsCompleted: 50, avgRating: 7, activeDays: 15 },
      config,
    );
    const capped = calculateScoreContributions(
      { totalEarnings: 2500, jobsCompleted: 50, avgRating: 5, activeDays: 15 },
      config,
    );

    expect(over.rating).toBe(capped.rating);
  });

  it("never exceeds maxScore in the total", () => {
    const total = calculateTotalScore(
      { earnings: 35, jobs: 25, rating: 30, activeDays: 10 },
      config,
    );

    expect(total).toBeLessThanOrEqual(config.maxScore);
  });
});