# 📦 Publishing the Aegis Suite to GitHub Packages

Both **Aegis-Telemetry** and **Aegis-Runtime** publish to GitHub Packages, the Maven
registry built into GitHub. Snapshots are fully supported (docs.github.com: *"GitHub
Packages supports SNAPSHOT versions of Apache Maven"*).

| Project | Coordinates | Registry URL |
| :--- | :--- | :--- |
| Aegis-Telemetry | `com.aegis.telemetry:*:0.1.0-SNAPSHOT` | `https://maven.pkg.github.com/Nithish-1622/Aegis-Telemetry` |
| Aegis-Runtime | `dev.nith:demo:0.0.1-SNAPSHOT` | `https://maven.pkg.github.com/Nithish-1622/Aegis-Runtime` |

> One registry URL per **repository**. A package published from repo A cannot be pushed to
> repo B's URL — each project gets its own `distributionManagement`.

---

## 1. One-time setup

### 1.1 Create a Personal Access Token

GitHub → Settings → Developer settings → **Personal access tokens → Fine-grained tokens**:

- **Repository access**: `Aegis-Telemetry` + `Aegis-Runtime`
- **Permissions → Packages**: `Read and write`
  (Classic token alternative: scopes `write:packages` + `read:packages`)

### 1.2 Create `~/.m2/settings.xml`

`%USERPROFILE%\.m2\settings.xml` on Windows. The `<id>` **must** be `github` — it is
matched against the `<id>` in every `distributionManagement` / `<repository>` block.

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <!-- PAT: ghp_... (classic) or github_pat_... (fine-grained) -->
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

⚠️ Never commit this file. Keep the token out of git.

---

## 2. Publish Aegis-Telemetry

Already configured in this repo — [`pom.xml`](../pom.xml) declares
`distributionManagement` → `maven.pkg.github.com/Nithish-1622/Aegis-Telemetry`, plus
`maven-source-plugin` / `maven-javadoc-plugin` so every artifact ships with
`-sources.jar` and `-javadoc.jar`.

```bash
mvn clean deploy -DskipTests
```

**What gets published** (7 libraries + the parent POM):

```
com.aegis.telemetry:aegis-telemetry       (parent pom)
com.aegis.telemetry:shared-contracts      jar + sources + javadoc
com.aegis.telemetry:trace-engine          jar + sources + javadoc
com.aegis.telemetry:runtime-sdk           jar + sources + javadoc
com.aegis.telemetry:instrumentation-engine jar + sources + javadoc
com.aegis.telemetry:event-publisher       jar + sources + javadoc
com.aegis.telemetry:service-registry      jar + sources + javadoc
com.aegis.telemetry:runtime-config        jar + sources + javadoc
```

**What is deliberately *not* published** — runnable applications, not libraries
(`maven.deploy.skip=true` in their POMs):

- `telemetry-bootstrap` — the boot app
- `failure-simulator` — benchmark harness
- `demo-services` + `gateway/order/payment/inventory-service`

### Recommended: always deploy from a clean tree

```bash
mvn clean deploy -DskipTests
```

Stale IDE-generated `.class` files (Eclipse "unresolved compilation" stubs) in `target/`
survive incremental builds and break `surefire` test discovery with
`TestEngine with ID 'junit-jupiter' failed to discover tests`. `clean` removes them.

---

## 3. Consume Aegis-Telemetry from any other project

**Both** blocks are required — GitHub Packages is not part of Maven's default
repositories, and it requires authentication even for public packages.

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/Nithish-1622/Aegis-Telemetry</url>
  </repository>
</repositories>
<pluginRepositories>
  <pluginRepository>
    <id>github</id>
    <url>https://maven.pkg.github.com/Nithish-1622/Aegis-Telemetry</url>
  </pluginRepository>
</pluginRepositories>

<dependency>
  <groupId>com.aegis.telemetry</groupId>
  <artifactId>shared-contracts</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Authentication reuses the same `<server><id>github</id>` entry from §1.2 — the `id`
matching across `distributionManagement`, `repositories` and `settings.xml` is what makes
Maven attach your token.

### Aegis-Runtime should depend on the contracts

Once published, `K:/Aegis-Runtime` replaces its re-declared event model with:

```xml
<dependency>
  <groupId>com.aegis.telemetry</groupId>
  <artifactId>shared-contracts</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

plus the `<repositories>` block above. This is the contract boundary the architecture
doc promises — one immutable `RuntimeEvent` definition shared by both systems, instead of
two copies that can drift.

---

## 4. Publish Aegis-Runtime

`K:/Aegis-Runtime/pom.xml` needs four changes.

### 4.1 `distributionManagement`

```xml
<distributionManagement>
  <repository>
    <id>github</id>
    <name>GitHub Packages — Aegis Runtime (releases)</name>
    <url>https://maven.pkg.github.com/Nithish-1622/Aegis-Runtime</url>
  </repository>
  <snapshotRepository>
    <id>github</id>
    <name>GitHub Packages — Aegis Runtime (snapshots)</name>
    <url>https://maven.pkg.github.com/Nithish-1622/Aegis-Runtime</url>
  </snapshotRepository>
</distributionManagement>
```

### 4.2 Keep the thin jar as the main artifact — 🚨 critical

The current POM declares `spring-boot-maven-plugin` with a `<mainClass>`, which binds the
`repackage` goal. That **replaces** the jar with a fat executable whose classes sit in
`BOOT-INF/classes/` — invisible to any consumer's compile classpath. Published as-is,
`dev.nith:demo` would be unusable as a dependency.

Add a `<classifier>` so both artifacts coexist:

```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <mainClass>com.aegis.runtime.RuntimeIntelligenceApplication</mainClass>
    <!-- thin jar = main artifact (importable); fat jar = -boot.jar -->
    <classifier>boot</classifier>
  </configuration>
</plugin>
```

Result: `demo-0.0.1-SNAPSHOT.jar` (library) and `demo-0.0.1-SNAPSHOT-boot.jar` (runnable).

### 4.3 Fill in the empty POM metadata

GitHub Packages tolerates `<name/>`, `<description/>`, `<url/>`, `<licenses/>`,
`<developers/>`, `<scm/>` as written — but Maven Central rejects empty elements outright,
and the package page renders blank without them. Filling them now makes a later Central
migration a no-op:

```xml
<name>Aegis Runtime Intelligence</name>
<description>Runtime intelligence analytics: consumes Aegis telemetry events from Kafka,
reconstructs request timelines, and detects anomalies.</description>
<url>https://github.com/Nithish-1622/Aegis-Runtime</url>
<scm>
  <connection>scm:git:https://github.com/Nithish-1622/Aegis-Runtime.git</connection>
  <developerConnection>scm:git:ssh://git@github.com/Nithish-1622/Aegis-Runtime.git</developerConnection>
  <url>https://github.com/Nithish-1622/Aegis-Runtime</url>
</scm>
```

### 4.4 Optionally attach sources + javadoc

Copy the two plugin blocks from this repo's root [`pom.xml`](../pom.xml) into
Aegis-Runtime's `<build><plugins>`.

### 4.5 Deploy

```bash
cd K:/Aegis-Runtime
mvn clean deploy -DskipTests
```

---

## 5. ⚠️ Dependency-management warning

Aegis-Telemetry builds against **Spring Boot 3.5.0**; Aegis-Runtime's parent is
**`spring-boot-starter-parent:4.0.5`**. Because a consumer resolves Runtime's POM *and its
parent chain*, depending on `dev.nith:demo` drags Spring Boot 4.x artifacts into the graph.
Inside a Boot 3.5 service that produces version conflicts at runtime.

Options, cheapest first:

1. Drop Aegis-Runtime to `spring-boot-starter-parent:3.5.0` to match Telemetry.
2. Keep Boot 4, but mark the framework-heavy dependencies (`spring-boot-starter-web*`,
   `data-jpa`, `flyway`, `postgresql`) `<optional>true</optional>` so consumers opt in.
3. Split Runtime: publish a lean `runtime-contracts` / `runtime-core` library, keep the
   Boot 4 app unpublished.

---

## 6. First-publish checklist

1. `mvn clean deploy -DskipTests` in Aegis-Telemetry → package appears under
   `https://github.com/Nithish-1622?tab=packages`.
2. If the package is private, its **Connect** link must point at the owning repo, and your
   account needs **read** access to download / **write** to publish.
3. Confirm resolution from a scratch project using §3.
4. Apply §4 to Aegis-Runtime, deploy, verify `shared-contracts` resolves there.
5. **Publishing is automated** — `.github/workflows/publish.yml` exists in both repos:

   | Trigger | What it publishes |
   | :--- | :--- |
   | Push to `main` | `0.1.0-SNAPSHOT` / `0.0.1-SNAPSHOT` |
   | Tag `v1.2.3` | `1.2.3` (via `versions-maven-plugin:set` on all 15 POMs) |
   | `workflow_dispatch` | Whatever the current POM version is |

   Auth uses the ephemeral `GITHUB_TOKEN` (`permissions: packages: write`) written into
   `~/.m2/settings.xml` by `actions/setup-java` — **no PAT ever leaves GitHub**. Every run
   finishes by resolving the just-published artifact from an *empty* local repository, so
   a silent "uploaded but not consumable" failure can't pass CI. Local `mvn deploy` from
   §2/§4 remains the manual fallback and needs the PAT from §1.
