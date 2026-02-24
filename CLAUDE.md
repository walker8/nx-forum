# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NX Forum is a modern open-source forum system built with Spring Boot + Nuxt. It uses **COLA (Clean Object-Oriented and
Layered Architecture)** framework supporting both DDD and MVC patterns.

**Important**: This is a **testing version** (not production-ready). Expected official release in 2026.
**Test Account**: admin/12345678

---

## Architecture

The backend follows COLA framework with four layers:

```
Adapter Layer (适配层)      ← HTTP request handling
Application Layer (应用层)   ← Business orchestration
Domain Layer (领域层)        ← Core business logic (DDD only)
Infrastructure Layer (基础层) ← Data persistence
```

### DDD vs MVC Pattern Choice

- **Use DDD mode** for complex write scenarios with business rules (e.g., post publishing, comment management, user
  registration)
    - Has Domain layer with Entities (*E), Value Objects (*V), Domain Services, Gateway interfaces
    - Application layer orchestrates domain objects
    - Infrastructure implements Gateway interfaces

- **Use MVC mode** for simple CRUD or query scenarios (e.g., config queries, data dictionaries, simple queries)
    - No Domain layer
    - Application layer directly calls Infrastructure Mappers

**Key Rule**: Module-to-module calls should only go through Application layer, never Domain or Infrastructure layers
across modules.

---

## Project Structure

```
nx-forum/
├── nx-forum-backend/              # Spring Boot backend
│   ├── nx-biz-forum/              # Forum business module
│   │   ├── nx-forum-adapter/      # Interface layer (web/admin/mobile/wap)
│   │   ├── nx-forum-app/          # Application services
│   │   ├── nx-forum-domain/       # Domain layer (DDD mode only)
│   │   ├── nx-forum-infrastructure/ # Infrastructure layer
│   │   └── nx-forum-start/        # Startup module
│   └── nx-platform/               # Platform foundation modules
│       ├── nx-common/             # Common utilities
│       ├── nx-module-cache/       # Caching module
│       ├── nx-module-config/      # Configuration center
│       ├── nx-module-mail/        # Mail module
│       ├── nx-module-ratelimit/   # Rate limiting
│       ├── nx-module-sms/         # SMS module
│       └── nx-module-uc/          # User center module
└── nx-forum-nuxt/                 # Nuxt frontend
    ├── apis/                      # API layer
    ├── components/                # Vue components
    ├── composables/               # Composition functions
    ├── layouts/                   # Layout components
    ├── pages/                     # Page files
    ├── stores/                    # Pinia stores
    ├── types/                     # TypeScript definitions
    └── utils/                     # Utility functions
```

---

## Development Commands

### Backend (Java 17 + Maven)

#### Quick Start with H2 (Embedded Database)

```bash
cd nx-forum-backend/nx-biz-forum/nx-forum-start
# Modify application.yml: set spring.profiles.active=dev-h2
mvn spring-boot:run
```

H2 database requires no installation, data stored in `./data/nx-forum.mv.db` file.
H2 Console: http://localhost:8083/nx-forum/h2-console (user: sa, password: empty)

#### Standard Start with MySQL

```bash
cd nx-forum-backend
mvn clean install           # Build all modules
cd nx-biz-forum/nx-forum-start
# Ensure MySQL is running and database exists
mvn spring-boot:run         # Start backend server (default: http://localhost:8083)
```

Backend API docs: `http://localhost:8083/nx-forum/doc.html`

### Frontend (Node.js v20+ + Yarn)

```bash
cd nx-forum-nuxt
yarn install                # Install dependencies
yarn dev                    # Start dev server
yarn build                  # Build for production
yarn preview                # Preview production build
yarn zip                    # Create deployment package (excludes uploads)
```

### One-click Build

```bash
./script/build.sh           # Build both frontend and backend
```

---

## Naming Conventions

### Data Objects

| Type              | Suffix                  | Usage                                      |
|-------------------|-------------------------|--------------------------------------------|
| Command           | `*Cmd`                  | Write operation parameters (Adapter input) |
| Query             | `*Query` / `*PageQuery` | Query parameters (Adapter input)           |
| View Object       | `*VO`                   | Response to frontend                       |
| Domain Entity     | `*E`                    | Domain model (DDD only)                    |
| Value Object      | `*V`                    | Immutable domain object (DDD only)         |
| Persistent Object | `*PO`                   | Database mapping (Infrastructure)          |

### Classes

- Controllers: `*Controller`
- Applications: `*Application`
- Domain Services: `*DomainService` (DDD only)
- Gateways: `*Gateway` interface / `*GatewayImpl` implementation (DDD only)
- Mapper interfaces: `I*Service`
- Mapper implementations: `*ServiceImpl`

---

## Configuration

### Backend Configuration

Main config: `nx-forum-backend/nx-biz-forum/nx-forum-start/src/main/resources/application.yml`

**Important settings**:

- Database: MySQL 8.0+ (Flyway migrations)
- Redis: 6.0+ (optional)
- File upload path: Set to frontend's public directory for local development:
  ```yaml
  nx.file.upload.path=/path/to/nx-forum-nuxt/public/
  ```

### Frontend Configuration

Main config: `nx-forum-nuxt/nuxt.config.ts`

- Auto-imports: Vue composables and Element Plus components auto-imported
- State persistence: Pinia stores persist in localStorage

---

## Common Issues

**H2 and MySQL database switching**:

- Development environment defaults to MySQL (requires MySQL installed and running)
- For quick startup, switch to H2 embedded database (no installation required)
- Modify `application.yml`: change `spring.profiles.active` to `dev-h2` or `dev`
- Note: H2 and MySQL use separate migration scripts, tables auto-initialize on first startup

**Flyway validation error after updates**:

- Testing version has incompatible migrations. Delete all database tables and restart.

**App Client Integration**:

### User-Agent Format for App Clients

App clients must include the following prefix in their User-Agent:

```
NXForumApp/<version> (Platform)
```

**Examples:**

- Android: `NXForumApp/1.0.0 (Android 12) Dalvik/2.1.0`
- iOS: `NXForumApp/1.0.0 (iPhone 15 iOS 17) Mobile/15E148`

### Required Headers

App clients **MUST** include the following HTTP headers:

1. **X-App-Version**: App版本号（主要APP检测依据）
   ```
   X-App-Version: 1.0.0
   ```

2. **X-Device-Id**: 设备唯一标识（用于设备去重和统计，不参与终端类型判断）
   ```
   X-Device-Id: {unique_device_id}
   ```

3. **User-Agent**: 包含 `NXForumApp/` 前缀（辅助APP检测）
   ```
   User-Agent: NXForumApp/1.0.0 (Android 12) Dalvik/2.1.0
   ```

### Terminal Type Detection Logic

The system uses a two-tier detection strategy:

1. **Primary**: X-App-Version header exists → APP
2. **Secondary**: User-Agent contains `NXForumApp/` → APP
3. **Default**: Mobile keyword detection → MOBILE, else PC

**Note**: deviceId is NOT used for terminal type detection, only for device deduplication and statistics.

---

## Technology Stack

### Backend

- Java 17
- Spring Boot 3.5.7
- MyBatis-Plus 3.5.13
- MySQL 8.0+ or H2 2.3.232 (embedded, dev/test only) with Flyway 11.10.0
- Redis 6.0+ (optional) with JetCache 2.7.8
- COLA Architecture 5.0.0
- Knife4j 4.5.0 (API docs)
- MapStruct Plus 1.4.8

### Frontend

- Nuxt 4 with TypeScript 5.9
- Tailwind CSS
- Element-Plus 2.11 (desktop)
- Vant UI 4.9 (mobile)
- TipTap 3.11 (rich text editor)
- Pinia 3.0.4 with persistence
- Yarn 1.22.22

---

## Key Development Notes

1. **Architecture**: Follow COLA framework guidelines. Choose DDD for complex business logic, MVC for simple operations.

2. **Layer separation**: No cross-layer dependencies. Domain layer only depends on Gateway interfaces.

3. **Module communication**: Only through Application layer. Never call other modules' Domain or Infrastructure layers
   directly.

4. **Database**: All POs extend BasePO with audit fields (createTime, updateTime, createBy, updateBy, isDeleted). Use
   logical deletion.

5. **Transactions**: Use `@Transactional` in Application layer, not Controller layer.

6. **Caching**: Use `GenericCache` utility. Prefer local cache (Caffeine), consider distributed cache (Redis) when
   needed.

7. **Validation**: Use `ValidationException` for business validation errors (4xx), `BusinessException` for system
   errors (5xx).

## Workflow Orchestration

### 1. Plan Node Default

- Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
- If something goes sideways, STOP and re-plan immediately – don't keep pushing
- Use plan mode for verification steps, not just building
- Write detailed specs upfront to reduce ambiguity

### 2. Subagent Strategy

- Use subagents liberally to keep main context window clean
- Offload research, exploration, and parallel analysis to subagents
- For complex problems, throw more compute at it via subagents
- One task per subagent for focused execution

### 3. Self-Improvement Loop

- After ANY correction from the user: update `tasks/lessons.md` with the pattern
- Write rules for yourself that prevent the same mistake
- Ruthlessly iterate on these lessons until mistake rate drops
- Review lessons at session start for relevant project

### 4. Verification Before Done

- Never mark a task complete without proving it works
- Diff behavior between main and your changes when relevant
- Ask yourself: "Would a staff engineer approve this?"
- Run tests, check logs, demonstrate correctness

### 5. Demand Elegance (Balanced)

- For non-trivial changes: pause and ask "is there a more elegant way?"
- If a fix feels hacky: "Knowing everything I know now, implement the elegant solution"
- Skip this for simple, obvious fixes – don't over-engineer
- Challenge your own work before presenting it

### 6. Autonomous Bug Fixing

- When given a bug report: just fix it. Don't ask for hand-holding
- Point at logs, errors, failing tests – then resolve them
- Zero context switching required from the user
- Go fix failing CI tests without being told how

---

## Task Management

1. **Plan First**: Write plan to `tasks/todo.md` with checkable items
2. **Verify Plan**: Check in before starting implementation
3. **Track Progress**: Mark items complete as you go
4. **Explain Changes**: High-level summary at each step
5. **Document Results**: Add review section to `tasks/todo.md`
6. **Capture Lessons**: Update `tasks/lessons.md` after corrections

---

## Core Principles

- **Simplicity First**: Make every change as simple as possible. Impact minimal code.
- **No Laziness**: Find root causes. No temporary fixes. Senior developer standards.
- **Minimal Impact**: Changes should only touch what's necessary. Avoid introducing bugs.