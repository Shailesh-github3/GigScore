# Secret Scan Report

**Scan Date:** September 20, 2026  
**Tools Used:** Gitleaks v8.30.1, Git History Diff Scanner, Working Tree Static Analysis  
**Repository:** Public Git Repository (`https://github.com/Shailesh-github3/GigScore.git`)  
**Branch:** `security/phase-a`  

---

## 1. Executive Summary

A comprehensive scan of the complete Git commit history and current working tree was performed. The primary critical finding is that a plaintext MySQL database password was committed to the repository in early commits and remained in documentation. Because this repository is public, this credential must be treated as completely compromised.

---

## 2. Secrets Discovered in Git History

| Commit Hash | Author | Date | File Path | Secret Type | Value / Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `4660c9c` | `hari7niv` | 2026-03-25 | `backend/gigscore/src/main/resources/application.properties` | MySQL Database Password (`spring.datasource.password`) | `<REDACTED>` (Compromised) |
| `4660c9c` | `hari7niv` | 2026-03-25 | `backend/gigscore/src/main/resources/application.properties` | Default Spring Security Admin Password (`spring.security.user.password`) | `admin` (Insecure default) |
| `a1b12d8` | `hari7niv` | 2026-03-25 | `README.md` | MySQL Database Password in documentation example block | `<REDACTED>` (Compromised) |
| `02a5bd9` | `hari7niv` | 2026-03-25 | `README.md` | JWT Header Example (`curl -H "Authorization: Bearer ..."`) | `YOUR_JWT_TOKEN` (Documentation placeholder) |
| `d8a50d4` | `Shailesh-github3` | 2026-07-25 | `backend/gigscore/src/test/resources/application-test.properties` | Dummy Test JWT Signing Secret (`jwt.secret`) | `testSecretKey...` (Test-only mock padding) |
| `d8a50d4` | `Shailesh-github3` | 2026-07-25 | `backend/gigscore/src/test/resources/application-test.properties` | Dummy Test Gemini Key (`gemini.api.key`) | `test-api-key-dummy` (Test-only mock value) |

---

## 3. Working Tree Configuration Audit (Current State)

### 3.1 `backend/gigscore/src/main/resources/application.properties`
* **Line 4:** `spring.datasource.password=${DB_PASSWORD:root}`  
  * *Risk:* Insecure default password fallback (`root`). If `DB_PASSWORD` environment variable is omitted in production, the application attempts to connect with password `root`.
  * *Remediation:* Remove the default fallback so that `spring.datasource.password=${DB_PASSWORD}` fails at startup if unset.
* **Line 14:** `jwt.secret=${JWT_SECRET}`  
  * *Status:* Externalized to environment variable. However, it currently lacks a startup validation check ensuring it is at least 32 bytes (256 bits).
* **Line 16:** `gemini.api.key=${GEMINI_API_KEY:}`  
  * *Status:* Externalized. Defaults to empty string, which safely triggers fallback chat behavior.
* **Lines 24-25:** `gigscore.seed-lender.email=${DEMO_LENDER_EMAIL:}` and `password=${DEMO_LENDER_PASSWORD:}`  
  * *Status:* Externalized. Safely defaults to blank (seeding skipped when empty).

### 3.2 `docker-compose.yml`
* **Line 5:** `MYSQL_ROOT_PASSWORD: ${DB_PASSWORD:-root}`  
  * *Risk:* Insecure default fallback (`root`) if `.env` is omitted.
* **Line 24:** `DB_PASSWORD: ${DB_PASSWORD:-root}`  
  * *Risk:* Insecure default fallback (`root`).
* *Remediation:* Require `.env` file and make `DB_PASSWORD` mandatory (`${DB_PASSWORD:?Set DB_PASSWORD in .env}`).

### 3.3 CI Workflows (`.github/workflows/ci.yml`)
* **Status:** Clean. The workflow executes `mvn test` (which loads the isolated H2 in-memory test profile) and `npm test` / `npm run build`. No credentials or API keys are embedded or required for CI execution.

### 3.4 `.env` Files
* **Status:** No active `.env` or `.env.local` files exist in the repository tree. A `.env.example` template is currently missing and must be added.

---

## 4. Mandatory Secret Rotation Action Items (For User)

The following credentials must be manually rotated by you immediately:

1. **MySQL Database Password:**
   * Change the password of the MySQL root user (and any application database user) on your local machine and on any remote database host.
   * If you reused `<REDACTED>` anywhere else (e.g., personal email, other servers, services), change it there immediately.
2. **Production JWT Signing Secret (`JWT_SECRET`):**
   * Generate a fresh, cryptographically strong random key (at least 32 characters / 256 bits, e.g., via `openssl rand -base64 32`).
   * Never commit this value to Git; place it only in your local uncommitted `.env` file.
3. **Google Gemini API Key (`GEMINI_API_KEY`):**
   * If you have ever created or used a live Gemini API key with this project, visit Google AI Studio, delete the existing key, and generate a new key.
