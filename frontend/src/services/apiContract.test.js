import { beforeEach, describe, expect, it, vi } from "vitest";
import httpClient from "./httpClient";
import { createUser, getUserDashboard, loginUser } from "./userService";
import { addGig, getPlatforms } from "./gigService";
import { getRecentActivities } from "./activityService";
import { askChatAssistant } from "./chatService";
import { getScore, getScoreConfig, getScoreHistory } from "./scoreService";
import { getWorker, getWorkers } from "./lenderService";

vi.mock("./httpClient", () => ({
  default: { get: vi.fn(), post: vi.fn() },
}));

describe("api contract", () => {
  beforeEach(() => vi.clearAllMocks());

  it("registers and logs in through versioned user endpoints", async () => {
    httpClient.post.mockResolvedValue({ data: { token: "t" } });

    await createUser({ name: "A", email: "a@x.com", password: "p" });
    expect(httpClient.post).toHaveBeenCalledWith("/api/v1/users", {
      name: "A",
      email: "a@x.com",
      password: "p",
    });

    await loginUser({ email: "a@x.com", password: "p" });
    expect(httpClient.post).toHaveBeenCalledWith("/api/v1/users/login", {
      email: "a@x.com",
      password: "p",
    });
  });

  it("fetches dashboards, gigs, platforms, activities and chat through versioned endpoints", async () => {
    httpClient.get.mockResolvedValue({ data: {} });
    httpClient.post.mockResolvedValue({ data: {} });

    await getUserDashboard(5);
    expect(httpClient.get).toHaveBeenCalledWith("/api/v1/users/5");

    await addGig({ userId: 5, platform: "Uber", amount: 10, rating: 5 });
    expect(httpClient.post).toHaveBeenCalledWith("/api/v1/gigs", {
      userId: 5,
      platform: "Uber",
      amount: 10,
      rating: 5,
    });

    await getPlatforms();
    expect(httpClient.get).toHaveBeenCalledWith("/api/v1/platforms");

    await getRecentActivities(5, 1, 10);
    expect(httpClient.get).toHaveBeenCalledWith("/api/v1/activity/5?page=1&size=10");

    await askChatAssistant([{ role: "user", text: "hi" }]);
    expect(httpClient.post).toHaveBeenCalledWith("/api/v1/chat/ask", {
      messages: [{ role: "user", text: "hi" }],
    });
  });

  it("fetches scores and lender views through versioned endpoints", async () => {
    httpClient.get.mockResolvedValue({ data: {} });

    await getScore(7);
    expect(httpClient.get).toHaveBeenCalledWith("/api/v1/score/7");

    await getScoreConfig();
    expect(httpClient.get).toHaveBeenCalledWith("/api/v1/score/config");

    await getScoreHistory(7);
    expect(httpClient.get).toHaveBeenCalledWith("/api/v1/score/7/history");

    await getWorkers();
    expect(httpClient.get).toHaveBeenCalledWith("/api/v1/lender/workers");

    await getWorker(9);
    expect(httpClient.get).toHaveBeenCalledWith("/api/v1/lender/workers/9");
  });
});