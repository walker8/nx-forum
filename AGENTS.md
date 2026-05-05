# Repository Guidelines

## Project Structure & Module Organization

- `nx-forum-backend/`: Spring Boot 3 backend using COLA-style modules. Forum business code is in `nx-biz-forum/` with `adapter`, `app`, `domain`, `infrastructure`, and `start` modules. Shared platform modules live under `nx-platform/`.
- `nx-forum-nuxt/`: Nuxt 4 frontend. Main code is organized in `pages/`, `components/`, `apis/`, `stores/`, `types/`, `utils/`, `assets/`, and `public/`.
- `docs/`, `doc/`, `openspec/`, `script/`, and `tasks/`: supporting documentation, specifications, scripts, and task notes.

## Build, Test, and Development Commands

Backend:

```bash
cd nx-forum-backend && mvn clean install
cd nx-forum-backend && mvn test
cd nx-forum-backend/nx-biz-forum/nx-forum-start && mvn spring-boot:run
```

Frontend:

```bash
cd nx-forum-nuxt && yarn install
cd nx-forum-nuxt && yarn dev
cd nx-forum-nuxt && yarn build
cd nx-forum-nuxt && yarn preview
```

`mvn clean install` builds all backend modules. `mvn test` runs JUnit tests. `yarn dev` starts Nuxt locally, `yarn build` creates the production build, and `yarn preview` serves the built app.

## Coding Style & Naming Conventions

Use English identifiers and keep comments or project documentation in Chinese when helpful. Java classes use `PascalCase`; methods and fields use `camelCase`; constants use `UPPER_SNAKE_CASE`; booleans should read as `is*` or `has*`. Backend DTO suffixes should follow local conventions such as `Cmd`, `Query`, `VO`, `E`, `V`, and `PO`.

For Vue, use Vue 3 Composition API with `<script setup lang="ts">`, typed props, PascalCase component names, and composables for shared logic. Follow existing Tailwind, Element Plus, and Vant patterns.

## Testing Guidelines

Backend tests use JUnit under each module's `src/test/java`; name test classes `*Test`. Run targeted checks with:

```bash
cd nx-forum-backend && mvn test -Dtest=ClassName#methodName
```

Frontend tests should use colocated `*.test.ts` or `*.spec.ts` files when added. Add regression coverage before bug fixes or cleanup work.

## Commit & Pull Request Guidelines

Git history uses scoped Conventional Commit subjects, for example `fix(ssr): ...`, `perf(nuxt): ...`, and `feat(db): ...`. Keep subjects imperative and concise.

Pull requests should include the problem, key changes, verification commands, linked issues, and screenshots for UI changes.

## Security & Configuration Tips

Do not commit secrets, uploads, build outputs, keystores, or generated runtime state. Backend configuration is in `nx-forum-start/src/main/resources/application.yml`; API docs are available after startup at `/nx-forum/doc.html`.
