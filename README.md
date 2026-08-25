# TaskApp (Multitask v1)

The first generation of [Multitask](https://github.com/jckyii/MultiTask) — a web task manager I designed, built, and deployed as my first full-stack project (April–June 2026). It is still live and still serving its original users.

> 🔗 **Live app:** https://taskmanager-gcvv.onrender.com · **The successor (v2):** [jckyii/MultiTask](https://github.com/jckyii/MultiTask) — a native, offline-first rebuild of this product.

<p align="center">
  <img src="screenshots/tasks.png" width="88%" alt="TaskApp — status-colored task cards grouped by status, with priority badges and category/subject pills" />
</p>

## What it does

- Tasks carry a title, description, due date and time, a **category**, a **subject**, and an optional **priority tier** (1st / 2nd / 3rd).
- Every task has a live **status** — Ongoing, Urgent, Overdue, or Completed — computed in the user's own timezone. "Urgent" means due within your threshold, which is a per-user setting (1–720 hours, default 48).
- Status drives the whole UI: white / orange / red / green card backgrounds, status badges, and colored pills for category and subject.
- The list can be **grouped five ways** (status, date, subject, category, priority), searched as you type, and filtered by category or subject. Completed tasks stay in a collapsed section at the top.
- Categories and subjects are **created by the user**, each with a color picker; deleting one removes it from every task, behind a confirmation.
- A **month calendar** (FullCalendar) shows dated tasks colored by subject; clicking a day lists that day's tasks, and clicking an entry opens the edit dialog in place.
- **Accounts:** email + password with BCrypt hashing, email **verification links** (24-hour single-use tokens, with resend), re-authentication before sensitive changes, email changes confirmed from the *new* address, and a remember-me cookie. Every query is scoped to the signed-in user.
- **Settings:** display name, email, password, urgency threshold, and a searchable timezone picker (with a banner that offers to update it when your browser reports a different zone).

## Stack

Java 24 · Spring Boot 4 · Vaadin Flow 25 (server-rendered UI) · Spring Security · Spring Data JPA / Hibernate · PostgreSQL on Supabase · Spring Mail (verification emails) · Docker → deployed on Render.

## Numbers

~4,600 lines of Java · 65 commits across 14 pull requests over two months · 3 database tables · 8 routes.

## Why it was rebuilt

Using it every day exposed three limits: it only works online, the server-rendered framework restricted the interaction design I wanted, and a task manager really lives on a phone. The rebuild became [Multitask v2](https://github.com/jckyii/MultiTask) — same database, same product DNA (the status colors and pills you see above started here), redesigned from the ground up as a native, offline-first app. A v1 account signs into v2 with the same email and its tasks follow automatically.

## Running it locally

```bash
./mvnw    # starts Spring Boot + Vaadin dev mode on :8080
```

Requires environment variables for the database and mailer: `DB_URL`, `DB_USER`, `DB_PASSWORD` (Postgres — use Supabase's transaction-mode pooler), `MAIL_USERNAME`, `MAIL_PASSWORD` (a Gmail app password), and optionally `APP_BASE_URL` so verification emails link to the right host.
