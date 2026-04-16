# Publishing Guide

This repository contains multiple Maven modules, but **only two are publishable as packages**: `infra-core` and `infra-spring-boot`.

- ✅ **Publishable**: `infra-core`, `infra-spring-boot`
- ❌ **Not published**: `api-gateway`, `order` (these are application modules)

---

## Module Overview

### `infra-core` (Pure Java Library)
- **Framework-agnostic** - zero Spring Framework dependencies
- **Reusable** - can be used in standalone Java projects, non-Spring microservices, CLI tools, etc.
- **Latest version**: `1.0.0`
- **Published to**: Maven Central Repository (or your private repository)

### `infra-spring-boot` (Spring Boot Integration)
- **Optional** - Spring Boot auto-configuration layer on top of `infra-core`
- **Reusable** - for Spring Boot 3.x+ applications
- **Latest version**: `1.0.0`
- **Published to**: Maven Central Repository (or your private repository)

### `api-gateway` & `order` (Application Modules)
- Application code, not libraries
- Deployment skipped automatically via `<skip>true</skip>` in maven-deploy-plugin
- Build succeeds, but `mvn deploy` will skip these modules

---

## Publishing to Maven Central

### Prerequisites

1. **OSSRH Account** (Open Source Software Repository Hosting)
   - Register at: https://central.sonatype.org/
   - Request access for your group ID (`com.emi`)

2. **GPG Key for Signing**
   ```bash
   gpg --full-generate-key   # Generate a new GPG key pair
   gpg --list-keys            # List your keys
   ```

3. **Maven Settings** (`~/.m2/settings.xml`)
   ```xml
   <settings>
     <servers>
       <server>
         <id>ossrh</id>
         <username>your-sonatype-username</username>
         <password>your-sonatype-password</password>
       </server>
     </servers>
     
     <profiles>
       <profile>
         <id>ossrh</id>
         <activation>
           <activeByDefault>true</activeByDefault>
         </activation>
         <properties>
           <gpg.executable>gpg</gpg.executable>
           <gpg.passphrase>your-gpg-passphrase</gpg.passphrase>
         </properties>
       </profile>
     </profiles>
   </settings>
   ```

4. **Update Repository URLs** in [pom.xml](pom.xml)
   - `<id>ossrh</id>` - already configured
   - URLs point to: 
     - Release: `https://oss.sonatype.org/service/staging/deploy/maven2/`
     - Snapshots: `https://oss.sonatype.org/content/repositories/snapshots`

5. **Update SCM and Developer Info**
   Update these in the POMs before publishing:
   ```bash
   # In parent pom.xml and each publishable module (infra-core, infra-spring-boot)
   <url>https://github.com/HimanshuKushwahadev27/distributed-rate-limiter</url>
   <scm>
     <url>https://github.com/HimanshuKushwahadev27/distributed-rate-limiter</url>
     <connection>scm:git:git://github.com/HimanshuKushwahadev27/distributed-rate-limiter.git</connection>
     <developerConnection>scm:git:ssh://git@github.com/HimanshuKushwahadev27/distributed-rate-limiter.git</developerConnection>
   </scm>
   ```

---

## Publishing Steps

### 1. Clean Build (Skip Tests)
```bash
mvn clean install -DskipTests
```

### 2. Verify Modules to Deploy
Only `infra-core` and `infra-spring-boot` should be deployed:
```bash
mvn deploy -DskipTests -P release -D skip.installnoether=true
```

Expected output:
- ✅ `infra-core`: Sources + Javadoc + JAR + GPG signatures uploaded
- ✅ `infra-spring-boot`: Sources + Javadoc + JAR + GPG signatures uploaded
- ⏭️ `api-gateway`: **SKIPPED** (skip=true in maven-deploy-plugin)
- ⏭️ `order`: **SKIPPED** (skip=true in maven-deploy-plugin)

### 3. View Deployment Details

**Without GPG signing (snapshot release to testing):**
```bash
mvn deploy -DskipTests
```

**With GPG signing (release to Maven Central production):**
```bash
mvn deploy -DskipTests -P release
```

---

## Verifying Published Packages

### Maven Central Search
https://central.sonatype.com/search?q=com.emi

### Local Repository Check
```bash
ls ~/.m2/repository/com/emi/
ls ~/.m2/repository/com/emi/infra-core/
ls ~/.m2/repository/com/emi/infra-spring-boot/
```

### Using Published Packages in Another Project

#### For `infra-core` (Pure Java)
```xml
<dependency>
    <groupId>com.emi</groupId>
    <artifactId>infra-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

Usage in standalone Java:
```java
InfraCoreFactory factory = new InfraCoreFactory(redisTemplate, stringTemplate);
RateLimiterStore limiter = factory.createRateLimiterStore();
```

#### For `infra-spring-boot` (Spring Boot)
```xml
<dependency>
    <groupId>com.emi</groupId>
    <artifactId>infra-spring-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

Usage in Spring Boot app (automatic bean wiring):
```java
@Service
public class MyService {
    public MyService(RateLimiterStore limiter) { // Auto-wired
        this.limiter = limiter;
    }
}
```

---

## Repository Setup for Private Maven Server

If publishing to a private Maven repository (Artifactory, Nexus, etc.):

1. **Update Distribution Management** in [pom.xml](pom.xml):
   ```xml
   <distributionManagement>
     <repository>
       <id>my-releases</id>
       <url>https://your-nexus.com/repository/maven-releases/</url>
     </repository>
     <snapshotRepository>
       <id>my-snapshots</id>
       <url>https://your-nexus.com/repository/maven-snapshots/</url>
     </snapshotRepository>
   </distributionManagement>
   ```

2. **Add credentials** to `~/.m2/settings.xml`:
   ```xml
   <server>
     <id>my-releases</id>
     <username>your-username</username>
     <password>your-password</password>
   </server>
   ```

3. **Deploy**:
   ```bash
   mvn clean install -DskipTests
   mvn deploy -DskipTests
   ```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Publish Packages

on:
  push:
    tags:
      - 'v[0-9]+.[0-9]+.[0-9]+'

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Set up GPG
        run: |
          mkdir -p ~/.gnupg
          echo "${{ secrets.GPG_SECRET_KEY }}" | base64 -d > ~/.gnupg/private.key
          gpg --import ~/.gnupg/private.key
      
      - name: Clean Build
        run: mvn clean install -DskipTests
      
      - name: Publish to Maven Central
        run: mvn deploy -DskipTests -P release
        env:
          SONATYPE_USERNAME: ${{ secrets.SONATYPE_USERNAME }}
          SONATYPE_PASSWORD: ${{ secrets.SONATYPE_PASSWORD }}
          GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
```

---

## Troubleshooting

### Problem: All modules being deployed
**Solution**: Check that `api-gateway` and `order` have `<skip>true</skip>` in their `maven-deploy-plugin` config.

### Problem: GPG signing fails
**Solution**: Ensure GPG key is imported and passphrase is correct:
```bash
gpg --list-keys
export GPG_PASSPHRASE="your-passphrase"
```

### Problem: OSSRH credentials rejected
**Solution**: 
- Verify OSSRH account is active
- Check credentials in `~/.m2/settings.xml`
- Ensure group ID registration (`com.emi`) is approved

### Problem: Javadoc generation fails
**Solution**: Fix Javadoc errors in source files or disable strict mode in parent pom.xml:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-javadoc-plugin</artifactId>
  <configuration>
    <failOnError>false</failOnError>
  </configuration>
</plugin>
```

---

## Key Configuration Files Updated

| File | Changes |
|---|---|
| `pom.xml` | Added `<distributionManagement>`, deploy/source/javadoc/GPG plugins |
| `infra-core/pom.xml` | Updated metadata, added source/javadoc plugins, `<skip>false</skip>` |
| `infra-spring-boot/pom.xml` | Updated metadata, added source/javadoc plugins, `<skip>false</skip>` |
| `api-gateway/pom.xml` | Added `<skip>true</skip>` in maven-deploy-plugin |
| `order/pom.xml` | Added `<skip>true</skip>` in maven-deploy-plugin |

---

## Summary: How It Works

```
Parent POM (pom.xml)
├── Defines <distributionManagement> for all modules
├── Provides plugin templates (source, javadoc, GPG, deploy)
│
├── infra-core
│   └── Inherits plugins, skip=false → ✅ PUBLISHES
│
├── infra-spring-boot
│   └── Inherits plugins, skip=false → ✅ PUBLISHES
│
├── api-gateway
│   └── Inherits plugins, skip=true → ❌ SKIPS DEPLOYMENT
│
└── order
    └── Inherits plugins, skip=true → ❌ SKIPS DEPLOYMENT

When mvn deploy runs:
→ Only infra-core & infra-spring-boot artifacts go to Maven Central
→ api-gateway & order builds complete but deployments are skipped
```

---

## Quick Reference

```bash
# Clean build all modules (including non-publishable ones)
mvn clean install -DskipTests

# Deploy only publishable modules (skip api-gateway & order)
mvn deploy -DskipTests

# Deploy with GPG signing (for Maven Central release)
mvn deploy -DskipTests -P release

# Deploy locally to ~/.m2/repository only (no deployment to remote)
mvn clean install -DskipTests
```
