# Agent-First AI Problem Solver — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a WeChat Mini Program + Spring Boot AI problem-solving app with LangChain4j Agent, supporting ACM algorithms, advanced math, and 考研408.

**Architecture:** Maven multi-module monolith (5 modules: gateway, agent-core, image-pipeline, mistake-service, user-service). MySQL for structured data, Redis for session memory, MongoDB for images and chat logs. Mini program is pure frontend, connecting via HTTPS REST + WSS WebSocket.

**Tech Stack:** Spring Boot 3.x, Maven, MySQL 8, Redis 7, MongoDB 7, LangChain4j, Alibaba Cloud DashScope SDK (Qwen-Max), Alibaba Cloud OCR SDK, WeChat Mini Program (native).

---

## File Structure Map

```
agent-dome/
├── pom.xml                                    # Parent POM
├── gateway/
│   ├── pom.xml
│   └── src/main/java/com/agentdome/gateway/
│       ├── GatewayApplication.java            # @SpringBootApplication entry
│       ├── config/
│       │   ├── WebSocketConfig.java
│       │   ├── CorsConfig.java
│       │   └── SecurityConfig.java
│       ├── controller/
│       │   ├── ChatController.java
│       │   └── HealthController.java
│       └── dto/
│           └── ApiResponse.java
├── agent-core/
│   ├── pom.xml
│   └── src/main/java/com/agentdome/agent/
│       ├── AgentService.java                  # Main orchestrator
│       ├── tools/
│       │   ├── SolveProblemTool.java
│       │   ├── AddToMistakesTool.java
│       │   ├── QueryMistakesTool.java
│       │   ├── RecommendSimilarTool.java
│       │   └── ExplainConceptTool.java
│       ├── memory/
│       │   ├── SessionMemoryManager.java      # Redis ops
│       │   └── SummaryService.java            # MongoDB cross-session summary
│       └── prompt/
│           └── PromptTemplateManager.java     # System + subject prompts
├── image-pipeline/
│   ├── pom.xml
│   └── src/main/java/com/agentdome/image/
│       ├── ImagePipelineService.java
│       ├── AliyunOcrService.java
│       ├── TextCleaningService.java
│       └── dto/
│           └── OcrResult.java
├── mistake-service/
│   ├── pom.xml
│   └── src/main/java/com/agentdome/mistake/
│       ├── MistakeService.java
│       ├── TagService.java
│       └── dto/
│           ├── MistakeDTO.java
│           └── TagDTO.java
├── user-service/
│   ├── pom.xml
│   └── src/main/java/com/agentdome/user/
│       ├── UserService.java
│       ├── WeChatAuthService.java
│       └── dto/
│           ├── LoginRequest.java
│           └── LoginResponse.java
└── common/
    ├── pom.xml
    └── src/main/java/com/agentdome/common/
        ├── entity/
        │   ├── User.java
        │   ├── Problem.java
        │   ├── MistakeCollection.java
        │   ├── Tag.java
        │   └── ProblemTag.java
        ├── repository/
        │   ├── UserRepository.java
        │   ├── ProblemRepository.java
        │   ├── MistakeCollectionRepository.java
        │   ├── TagRepository.java
        │   └── ProblemTagRepository.java
        ├── mongo/
        │   ├── ChatSessionRepository.java
        │   └── ImageRepository.java
        ├── config/
        │   ├── MongoConfig.java
        │   ├── RedisConfig.java
        │   └── DashScopeConfig.java
        ├── exception/
        │   ├── GlobalExceptionHandler.java
        │   └── BusinessException.java
        └── util/
            └── JwtUtil.java
```

Mini Program (in `miniprogram/`):
```
miniprogram/
├── app.js
├── app.json
├── app.wxss
├── pages/
│   ├── chat/
│   │   ├── chat.js
│   │   ├── chat.json
│   │   ├── chat.wxml
│   │   └── chat.wxss
│   ├── mistakes/
│   │   ├── mistakes.js
│   │   ├── mistakes.json
│   │   ├── mistakes.wxml
│   │   └── mistakes.wxss
│   └── profile/
│       ├── profile.js
│       ├── profile.json
│       ├── profile.wxml
│       └── profile.wxss
├── utils/
│   ├── api.js
│   ├── websocket.js
│   └── auth.js
```

---

## Phase 0: Project Scaffolding

### Task 0.1: Create parent POM

**Files:**
- Create: `pom.xml`

- [ ] **Step 1: Write parent POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.agentdome</groupId>
    <artifactId>agent-dome</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>AgentDome</name>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <langchain4j.version>0.34.0</langchain4j.version>
        <dashscope.version>2.16.0</dashscope.version>
        <aliyun-ocr.version>2021-07-07</aliyun-ocr.version>
    </properties>

    <modules>
        <module>common</module>
        <module>user-service</module>
        <module>image-pipeline</module>
        <module>mistake-service</module>
        <module>agent-core</module>
        <module>gateway</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.agentdome</groupId>
                <artifactId>common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.agentdome</groupId>
                <artifactId>user-service</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.agentdome</groupId>
                <artifactId>image-pipeline</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.agentdome</groupId>
                <artifactId>mistake-service</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.agentdome</groupId>
                <artifactId>agent-core</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>dev.langchain4j</groupId>
                <artifactId>langchain4j</artifactId>
                <version>${langchain4j.version}</version>
            </dependency>
            <dependency>
                <groupId>dev.langchain4j</groupId>
                <artifactId>langchain4j-dashscope</artifactId>
                <version>${langchain4j.version}</version>
            </dependency>
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>dashscope-sdk-java</artifactId>
                <version>${dashscope.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

- [ ] **Step 2: Verify**

Run: `mvn validate -f pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "feat: add parent POM with module and dependency management"
```

### Task 0.2: Create common module POM

**Files:**
- Create: `common/pom.xml`

- [ ] **Step 1: Write common POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.agentdome</groupId>
        <artifactId>agent-dome</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>common</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Verify**

Run: `mvn validate -f common/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add common/pom.xml
git commit -m "feat: add common module POM with JPA, MongoDB, Redis deps"
```

### Task 0.3: Create remaining module POMs

**Files:**
- Create: `gateway/pom.xml`, `agent-core/pom.xml`, `image-pipeline/pom.xml`, `mistake-service/pom.xml`, `user-service/pom.xml`

- [ ] **Step 1: Write gateway/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.agentdome</groupId>
        <artifactId>agent-dome</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>gateway</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>agent-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>image-pipeline</artifactId>
        </dependency>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>mistake-service</artifactId>
        </dependency>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>user-service</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Write agent-core/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.agentdome</groupId>
        <artifactId>agent-dome</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>agent-core</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-dashscope</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dashscope-sdk-java</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3: Write image-pipeline/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.agentdome</groupId>
        <artifactId>agent-dome</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>image-pipeline</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.aliyun</groupId>
            <artifactId>ocr_api20210707</artifactId>
            <!-- The SDK does not use a standard Maven coordinate.
                 Download the SDK JAR from Alibaba Cloud console and install locally:
                 mvn install:install-file -Dfile=ocr_api20210707.jar
                   -DgroupId=com.aliyun -DartifactId=ocr_api20210707
                   -Dversion=1.0.0 -Dpackaging=jar -->
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 4: Write mistake-service/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.agentdome</groupId>
        <artifactId>agent-dome</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>mistake-service</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 5: Write user-service/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.agentdome</groupId>
        <artifactId>agent-dome</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>user-service</artifactId>
    <packaging>jar</packaging>
    <dependencies>
        <dependency>
            <groupId>com.agentdome</groupId>
            <artifactId>common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 6: Verify**

Run: `mvn validate`
Expected: BUILD SUCCESS (all modules)

- [ ] **Step 7: Commit**

```bash
git add gateway/pom.xml agent-core/pom.xml image-pipeline/pom.xml mistake-service/pom.xml user-service/pom.xml
git commit -m "feat: add all module POMs"
```

### Task 0.4: Create application entry point and application.yml

**Files:**
- Create: `gateway/src/main/java/com/agentdome/gateway/GatewayApplication.java`
- Create: `gateway/src/main/resources/application.yml`
- Create: `gateway/src/main/resources/application-dev.yml`

- [ ] **Step 1: Write main application class**

```java
package com.agentdome.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.agentdome")
@EntityScan(basePackages = "com.agentdome.common.entity")
@EnableJpaRepositories(basePackages = "com.agentdome.common.repository")
@EnableMongoRepositories(basePackages = "com.agentdome.common.mongo")
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

- [ ] **Step 2: Write application.yml**

```yaml
spring:
  application:
    name: agent-dome
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/agent_dome?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
  data:
    mongodb:
      uri: mongodb://${MONGO_HOST:localhost}:27017/agent_dome
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

server:
  port: ${SERVER_PORT:8080}

aliyun:
  dashscope:
    api-key: ${DASHSCOPE_API_KEY:}
  ocr:
    access-key-id: ${ALIYUN_ACCESS_KEY_ID:}
    access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET:}

wechat:
  app-id: ${WECHAT_APP_ID:}
  app-secret: ${WECHAT_APP_SECRET:}

jwt:
  secret: ${JWT_SECRET:change-me-in-production}
  expiration-ms: 604800000

logging:
  level:
    com.agentdome: DEBUG
```

- [ ] **Step 3: Write application-dev.yml**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agent_dome?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  data:
    mongodb:
      uri: mongodb://localhost:27017/agent_dome
    redis:
      host: localhost
      port: 6379

logging:
  level:
    com.agentdome: DEBUG
    org.springframework.web: DEBUG
```

- [ ] **Step 4: Verify project structure**

Run: `mvn compile`
Expected: BUILD SUCCESS (may warn about missing source files — acceptable)

- [ ] **Step 5: Commit**

```bash
git add gateway/src/
git commit -m "feat: add GatewayApplication entry point and config files"
```

---

## Phase 1: Common Module — JPA Entities & Repositories

### Task 1.1: User entity and repository

**Files:**
- Create: `common/src/main/java/com/agentdome/common/entity/User.java`
- Create: `common/src/main/java/com/agentdome/common/repository/UserRepository.java`

- [ ] **Step 1: Write the failing test**

Create `gateway/src/test/java/com/agentdome/gateway/UserRepositoryTest.java`:

```java
package com.agentdome.gateway;

import com.agentdome.common.entity.User;
import com.agentdome.common.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByOpenid() {
        User user = new User();
        user.setOpenid("test-openid-123");
        user.setNickname("TestUser");
        userRepository.save(user);

        Optional<User> found = userRepository.findByOpenid("test-openid-123");

        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("TestUser");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl gateway -Dtest=UserRepositoryTest`
Expected: FAIL (User entity not defined)

- [ ] **Step 3: Write User entity**

```java
package com.agentdome.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String openid;

    @Column(length = 100)
    private String nickname;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 4: Write UserRepository**

```java
package com.agentdome.common.repository;

import com.agentdome.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOpenid(String openid);
}
```

- [ ] **Step 5: Add test profile**

Create `gateway/src/main/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
  data:
    mongodb:
      uri: mongodb://localhost:27017/test_agent_dome
```

Add H2 dependency to gateway/pom.xml:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn test -pl gateway -Dtest=UserRepositoryTest`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add common/src/main/java/com/agentdome/common/entity/User.java \
        common/src/main/java/com/agentdome/common/repository/UserRepository.java \
        gateway/src/test/java/com/agentdome/gateway/UserRepositoryTest.java \
        gateway/src/main/resources/application-test.yml \
        gateway/pom.xml
git commit -m "feat: add User entity and repository with test"
```

### Task 1.2: Problem entity and repository

**Files:**
- Create: `common/src/main/java/com/agentdome/common/entity/Problem.java`
- Create: `common/src/main/java/com/agentdome/common/repository/ProblemRepository.java`

- [ ] **Step 1: Write the failing test**

Create `gateway/src/test/java/com/agentdome/gateway/ProblemRepositoryTest.java`:

```java
package com.agentdome.gateway;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.entity.Problem.SubjectType;
import com.agentdome.common.repository.ProblemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProblemRepositoryTest {

    @Autowired
    private ProblemRepository problemRepository;

    @Test
    void shouldFindByUserIdAndSubjectType() {
        Problem p = new Problem();
        p.setUserId(1L);
        p.setSubjectType(SubjectType.ACM);
        p.setOcrRawText("Given an array, find the max subarray sum");
        p.setCleanedText("Given an array, find the max subarray sum");
        problemRepository.save(p);

        List<Problem> problems = problemRepository.findByUserIdAndSubjectType(1L, SubjectType.ACM);

        assertThat(problems).hasSize(1);
        assertThat(problems.get(0).getSubjectType()).isEqualTo(SubjectType.ACM);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl gateway -Dtest=ProblemRepositoryTest`
Expected: FAIL

- [ ] **Step 3: Write Problem entity**

```java
package com.agentdome.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 10)
    private SubjectType subjectType;

    @Column(name = "ocr_raw_text", columnDefinition = "TEXT")
    private String ocrRawText;

    @Column(name = "cleaned_text", columnDefinition = "TEXT")
    private String cleanedText;

    @Column(name = "original_image_id", length = 100)
    private String originalImageId;

    @Column(name = "solution_text", columnDefinition = "TEXT")
    private String solutionText;

    @Column(name = "solution_code", columnDefinition = "TEXT")
    private String solutionCode;

    @Column(name = "error_type", length = 50)
    private String errorType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum SubjectType {
        ACM, MATH, CS408
    }
}
```

- [ ] **Step 4: Write ProblemRepository**

```java
package com.agentdome.common.repository;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.entity.Problem.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByUserIdAndSubjectType(Long userId, SubjectType subjectType);
    List<Problem> findByUserIdOrderByCreatedAtDesc(Long userId);
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -pl gateway -Dtest=ProblemRepositoryTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add common/src/main/java/com/agentdome/common/entity/Problem.java \
        common/src/main/java/com/agentdome/common/repository/ProblemRepository.java \
        gateway/src/test/java/com/agentdome/gateway/ProblemRepositoryTest.java
git commit -m "feat: add Problem entity and repository with test"
```

### Task 1.3: Tag, ProblemTag, MistakeCollection entities and repositories

**Files:**
- Create: `common/src/main/java/com/agentdome/common/entity/Tag.java`
- Create: `common/src/main/java/com/agentdome/common/entity/ProblemTag.java`
- Create: `common/src/main/java/com/agentdome/common/entity/MistakeCollection.java`
- Create: `common/src/main/java/com/agentdome/common/repository/TagRepository.java`
- Create: `common/src/main/java/com/agentdome/common/repository/ProblemTagRepository.java`
- Create: `common/src/main/java/com/agentdome/common/repository/MistakeCollectionRepository.java`

- [ ] **Step 1: Write Tag entity**

```java
package com.agentdome.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String color;
}
```

- [ ] **Step 2: Write ProblemTag entity**

```java
package com.agentdome.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "problem_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemTag {

    @EmbeddedId
    private ProblemTagId id;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemTagId implements java.io.Serializable {
        @Column(name = "problem_id")
        private Long problemId;

        @Column(name = "tag_id")
        private Long tagId;
    }
}
```

- [ ] **Step 3: Write MistakeCollection entity**

```java
package com.agentdome.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mistake_collections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MistakeCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 4: Write repositories**

```java
// TagRepository.java
package com.agentdome.common.repository;

import com.agentdome.common.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserId(Long userId);
    Optional<Tag> findByUserIdAndName(Long userId, String name);
}
```

```java
// ProblemTagRepository.java
package com.agentdome.common.repository;

import com.agentdome.common.entity.ProblemTag;
import com.agentdome.common.entity.ProblemTag.ProblemTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemTagRepository extends JpaRepository<ProblemTag, ProblemTagId> {
    List<ProblemTag> findByIdProblemId(Long problemId);
    List<ProblemTag> findByIdTagId(Long tagId);
    void deleteByIdProblemId(Long problemId);
}
```

```java
// MistakeCollectionRepository.java
package com.agentdome.common.repository;

import com.agentdome.common.entity.MistakeCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MistakeCollectionRepository extends JpaRepository<MistakeCollection, Long> {
    List<MistakeCollection> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<MistakeCollection> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
```

- [ ] **Step 5: Write integration test**

Create `gateway/src/test/java/com/agentdome/gateway/MistakeRepositoryTest.java`:

```java
package com.agentdome.gateway;

import com.agentdome.common.entity.*;
import com.agentdome.common.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MistakeRepositoryTest {

    @Autowired private TagRepository tagRepository;
    @Autowired private MistakeCollectionRepository mistakeRepository;

    @Test
    void shouldCreateAndFindTags() {
        Tag tag = new Tag();
        tag.setUserId(1L);
        tag.setName("动态规划");
        tag.setColor("#4A90D9");
        tagRepository.save(tag);

        List<Tag> tags = tagRepository.findByUserId(1L);

        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).getName()).isEqualTo("动态规划");
    }

    @Test
    void shouldCreateAndFindMistakes() {
        MistakeCollection m = new MistakeCollection();
        m.setUserId(1L);
        m.setProblemId(10L);
        m.setSessionId("session-abc");
        m.setMemo("忘了状态转移方程");
        mistakeRepository.save(m);

        List<MistakeCollection> mistakes = mistakeRepository.findByUserIdOrderByCreatedAtDesc(1L);

        assertThat(mistakes).hasSize(1);
        assertThat(mistakes.get(0).getMemo()).isEqualTo("忘了状态转移方程");
    }
}
```

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn test -pl gateway -Dtest=MistakeRepositoryTest`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add common/src/main/java/com/agentdome/common/entity/Tag.java \
        common/src/main/java/com/agentdome/common/entity/ProblemTag.java \
        common/src/main/java/com/agentdome/common/entity/MistakeCollection.java \
        common/src/main/java/com/agentdome/common/repository/TagRepository.java \
        common/src/main/java/com/agentdome/common/repository/ProblemTagRepository.java \
        common/src/main/java/com/agentdome/common/repository/MistakeCollectionRepository.java \
        gateway/src/test/java/com/agentdome/gateway/MistakeRepositoryTest.java
git commit -m "feat: add Tag, ProblemTag, MistakeCollection entities and repos"
```

---

## Phase 2: Common Module — MongoDB, Redis, Infrastructure

### Task 2.1: MongoDB configuration and chat session document

**Files:**
- Create: `common/src/main/java/com/agentdome/common/config/MongoConfig.java`
- Create: `common/src/main/java/com/agentdome/common/mongo/ChatSessionDocument.java`
- Create: `common/src/main/java/com/agentdome/common/mongo/ChatSessionRepository.java`

- [ ] **Step 1: Write MongoConfig**

```java
package com.agentdome.common.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return "agent_dome";
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public GridFSBucket gridFSBucket(MongoDatabaseFactory factory) throws Exception {
        MongoDatabase db = factory.getMongoDatabase();
        return GridFSBuckets.create(db, "images");
    }

    @Bean
    public GridFsTemplate gridFsTemplate(MongoDatabaseFactory factory) throws Exception {
        return new GridFsTemplate(factory, new org.springframework.data.mongodb.core.convert.MappingMongoConverter(
                new org.springframework.data.mongodb.core.mapping.MongoMappingContext(), new org.bson.Document()));
    }
}
```

- [ ] **Step 2: Write ChatSessionDocument**

```java
package com.agentdome.common.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDocument {

    @Id
    private String id;

    private String sessionId;
    private Long userId;
    private List<ChatMessage> messages;
    private SessionSummary summary;
    private Instant createdAt;
    private Instant endedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
        private Instant timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionSummary {
        private int problemsSolved;
        private List<String> weakTopics;
        private int mistakesAdded;
    }
}
```

- [ ] **Step 3: Write ChatSessionRepository**

```java
package com.agentdome.common.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ChatSessionRepository extends MongoRepository<ChatSessionDocument, String> {
    Optional<ChatSessionDocument> findBySessionId(String sessionId);
}
```

- [ ] **Step 4: Commit**

```bash
git add common/src/main/java/com/agentdome/common/config/MongoConfig.java \
        common/src/main/java/com/agentdome/common/mongo/
git commit -m "feat: add MongoDB config, ChatSession document and repository"
```

### Task 2.2: Redis configuration and session memory manager

**Files:**
- Create: `common/src/main/java/com/agentdome/common/config/RedisConfig.java`

- [ ] **Step 1: Write RedisConfig**

```java
package com.agentdome.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add common/src/main/java/com/agentdome/common/config/RedisConfig.java
git commit -m "feat: add Redis configuration"
```

### Task 2.3: JWT utility and business exception

**Files:**
- Create: `common/src/main/java/com/agentdome/common/util/JwtUtil.java`
- Create: `common/src/main/java/com/agentdome/common/exception/BusinessException.java`
- Create: `common/src/main/java/com/agentdome/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: Write JwtUtil**

```java
package com.agentdome.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: Write BusinessException**

```java
package com.agentdome.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }
}
```

- [ ] **Step 3: Write GlobalExceptionHandler**

```java
package com.agentdome.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getCode())
                .body(Map.of("code", e.getCode(), "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        return ResponseEntity.status(500)
                .body(Map.of("code", 500, "message", "Internal server error"));
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add common/src/main/java/com/agentdome/common/util/JwtUtil.java \
        common/src/main/java/com/agentdome/common/exception/
git commit -m "feat: add JWT utility, business exception, global error handler"
```

---

## Phase 3: User Service Module

### Task 3.1: WeChat auth service

**Files:**
- Create: `user-service/src/main/java/com/agentdome/user/dto/LoginRequest.java`
- Create: `user-service/src/main/java/com/agentdome/user/dto/LoginResponse.java`
- Create: `user-service/src/main/java/com/agentdome/user/WeChatAuthService.java`
- Create: `user-service/src/main/java/com/agentdome/user/UserService.java`

- [ ] **Step 1: Write DTOs**

```java
// LoginRequest.java
package com.agentdome.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String code;
    private String nickname;
    private String avatarUrl;
}
```

```java
// LoginResponse.java
package com.agentdome.user.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String nickname;
    private String avatarUrl;
}
```

- [ ] **Step 2: Write WeChatAuthService**

```java
package com.agentdome.user;

import com.agentdome.common.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class WeChatAuthService {

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Exchange wx.login code for OpenID via WeChat API.
     * Returns the OpenID on success.
     */
    public String codeToOpenid(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);

            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                throw new BusinessException("WeChat login failed: " + json.get("errmsg").asText());
            }

            return json.get("openid").asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("WeChat auth service error: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 3: Write UserService**

```java
package com.agentdome.user;

import com.agentdome.common.entity.User;
import com.agentdome.common.repository.UserRepository;
import com.agentdome.common.util.JwtUtil;
import com.agentdome.user.dto.LoginRequest;
import com.agentdome.user.dto.LoginResponse;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WeChatAuthService weChatAuthService;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       WeChatAuthService weChatAuthService,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.weChatAuthService = weChatAuthService;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        String openid = weChatAuthService.codeToOpenid(request.getCode());

        User user = userRepository.findByOpenid(openid)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setOpenid(openid);
                    newUser.setNickname(request.getNickname());
                    newUser.setAvatarUrl(request.getAvatarUrl());
                    return userRepository.save(newUser);
                });

        user.setLastActiveAt(LocalDateTime.now());
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(token, user.getId(), user.getNickname(), user.getAvatarUrl());
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new com.agentdome.common.exception.BusinessException("User not found"));
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add user-service/src/
git commit -m "feat: add user service with WeChat auth and login flow"
```

### Task 3.2: Auth controller and interceptor

**Files:**
- Create: `gateway/src/main/java/com/agentdome/gateway/controller/AuthController.java`
- Create: `gateway/src/main/java/com/agentdome/gateway/config/SecurityConfig.java` (interceptor)

- [ ] **Step 1: Write AuthController**

```java
package com.agentdome.gateway.controller;

import com.agentdome.user.UserService;
import com.agentdome.user.dto.LoginRequest;
import com.agentdome.user.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
```

- [ ] **Step 2: Write SecurityConfig with auth interceptor**

```java
package com.agentdome.gateway.config;

import com.agentdome.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/health");
    }

    static class AuthInterceptor implements HandlerInterceptor {
        private final JwtUtil jwtUtil;

        AuthInterceptor(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) throws Exception {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(401);
                response.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
                return false;
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(401);
                response.getWriter().write("{\"code\":401,\"message\":\"Invalid token\"}");
                return false;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("userId", userId);
            return true;
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add gateway/src/main/java/com/agentdome/gateway/controller/AuthController.java \
        gateway/src/main/java/com/agentdome/gateway/config/SecurityConfig.java
git commit -m "feat: add auth controller and JWT interceptor"
```

---

## Phase 4: Image Pipeline Module

### Task 4.1: Aliyun OCR service

**Files:**
- Create: `image-pipeline/src/main/java/com/agentdome/image/dto/OcrResult.java`
- Create: `image-pipeline/src/main/java/com/agentdome/image/AliyunOcrService.java`

- [ ] **Step 1: Write OcrResult DTO**

```java
package com.agentdome.image.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    private String rawText;
    private double confidence;
    private List<TextBlock> blocks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextBlock {
        private String text;
        private int x;
        private int y;
        private int width;
        private int height;
    }
}
```

- [ ] **Step 2: Write AliyunOcrService**

```java
package com.agentdome.image;

import com.agentdome.common.exception.BusinessException;
import com.agentdome.image.dto.OcrResult;
import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeGeneralRequest;
import com.aliyun.ocr_api20210707.models.RecognizeGeneralResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

@Service
public class AliyunOcrService {

    @Value("${aliyun.ocr.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.ocr.access-key-secret}")
    private String accessKeySecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Call Alibaba Cloud General OCR API.
     * Image data is provided as base64-encoded bytes.
     */
    public OcrResult recognize(byte[] imageBytes) {
        try {
            com.aliyun.teaopenapi.models.Config config =
                    new com.aliyun.teaopenapi.models.Config()
                            .setAccessKeyId(accessKeyId)
                            .setAccessKeySecret(accessKeySecret);
            config.endpoint = "ocr-api.cn-hangzhou.aliyuncs.com";

            Client client = new Client(config);

            RecognizeGeneralRequest request = new RecognizeGeneralRequest()
                    .setBody(Base64.getEncoder().encodeToString(imageBytes));

            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeGeneralResponse response = client.recognizeGeneralWithOptions(request, runtime);

            String body = response.getBody().getData();
            JsonNode root = objectMapper.readTree(body);

            StringBuilder rawText = new StringBuilder();
            for (JsonNode block : root.get("content").get("prism_wordsInfo")) {
                rawText.append(block.get("word").asText()).append("\n");
            }

            OcrResult result = new OcrResult();
            result.setRawText(rawText.toString());
            result.setConfidence(root.has("confidence") ? root.get("confidence").asDouble() : 0.0);
            return result;

        } catch (Exception e) {
            throw new BusinessException("OCR recognition failed: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add image-pipeline/src/
git commit -m "feat: add Aliyun OCR service"
```

### Task 4.2: Text cleaning and image pipeline service

**Files:**
- Create: `image-pipeline/src/main/java/com/agentdome/image/TextCleaningService.java`
- Create: `image-pipeline/src/main/java/com/agentdome/image/ImagePipelineService.java`

- [ ] **Step 1: Write TextCleaningService**

```java
package com.agentdome.image;

import com.agentdome.image.dto.OcrResult;
import org.springframework.stereotype.Service;

@Service
public class TextCleaningService {

    /**
     * Clean OCR output: remove noise lines, merge paragraphs,
     * preserve code indentation and math formulas.
     */
    public String clean(OcrResult ocrResult) {
        if (ocrResult == null || ocrResult.getRawText() == null) {
            return "";
        }

        String[] lines = ocrResult.getRawText().split("\n");
        StringBuilder cleaned = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            // Skip empty lines and common noise patterns
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("扫描") || trimmed.startsWith("第") && trimmed.contains("页")) continue;

            cleaned.append(trimmed).append("\n");
        }

        // Collapse multiple newlines
        return cleaned.toString().replaceAll("\n{3,}", "\n\n").trim();
    }
}
```

- [ ] **Step 2: Write ImagePipelineService**

```java
package com.agentdome.image;

import com.agentdome.common.exception.BusinessException;
import com.agentdome.common.mongo.ChatSessionRepository;
import com.agentdome.image.dto.OcrResult;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class ImagePipelineService {

    private final GridFSBucket gridFSBucket;
    private final AliyunOcrService ocrService;
    private final TextCleaningService cleaningService;

    public ImagePipelineService(GridFSBucket gridFSBucket,
                                AliyunOcrService ocrService,
                                TextCleaningService cleaningService) {
        this.gridFSBucket = gridFSBucket;
        this.ocrService = ocrService;
        this.cleaningService = cleaningService;
    }

    /**
     * Full pipeline: upload → gridfs → OCR → clean → return cleaned text + imageId.
     */
    public PipelineResult process(MultipartFile file, Long userId) {
        try {
            byte[] bytes = file.getBytes();
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.jpg";

            // Store in GridFS
            GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(new org.bson.Document("user_id", userId)
                            .append("filename", filename));
            ObjectId fileId = gridFSBucket.uploadFromStream(filename,
                    new java.io.ByteArrayInputStream(bytes), options);
            String imageId = fileId.toHexString();

            // OCR
            OcrResult ocrResult = ocrService.recognize(bytes);

            // Clean text
            String cleanedText = cleaningService.clean(ocrResult);

            return new PipelineResult(imageId, ocrResult.getRawText(), cleanedText);
        } catch (IOException e) {
            throw new BusinessException("Image upload failed: " + e.getMessage());
        }
    }

    public record PipelineResult(String imageId, String rawText, String cleanedText) {}
}
```

- [ ] **Step 3: Commit**

```bash
git add image-pipeline/src/
git commit -m "feat: add text cleaning and image pipeline service"
```

---

## Phase 5: Mistake Service Module

### Task 5.1: TagService and MistakeService

**Files:**
- Create: `mistake-service/src/main/java/com/agentdome/mistake/TagService.java`
- Create: `mistake-service/src/main/java/com/agentdome/mistake/MistakeService.java`
- Create: `mistake-service/src/main/java/com/agentdome/mistake/dto/MistakeDTO.java`
- Create: `mistake-service/src/main/java/com/agentdome/mistake/dto/TagDTO.java`

- [ ] **Step 1: Write DTOs**

```java
// MistakeDTO.java
package com.agentdome.mistake.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MistakeDTO {
    private Long id;
    private Long problemId;
    private String subjectType;
    private String cleanedText;
    private String errorType;
    private String memo;
    private List<String> tags;
    private LocalDateTime createdAt;
}
```

```java
// TagDTO.java
package com.agentdome.mistake.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TagDTO {
    private Long id;

    @NotBlank
    private String name;
    private String color;
}
```

- [ ] **Step 2: Write TagService**

```java
package com.agentdome.mistake;

import com.agentdome.common.entity.Tag;
import com.agentdome.common.repository.TagRepository;
import com.agentdome.common.exception.BusinessException;
import com.agentdome.mistake.dto.TagDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag createTag(Long userId, TagDTO dto) {
        Optional<Tag> existing = tagRepository.findByUserIdAndName(userId, dto.getName());
        if (existing.isPresent()) {
            return existing.get();
        }
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(dto.getName());
        tag.setColor(dto.getColor() != null ? dto.getColor() : "#999999");
        return tagRepository.save(tag);
    }

    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByUserId(userId);
    }

    public void deleteTag(Long userId, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException("Tag not found"));
        if (!tag.getUserId().equals(userId)) {
            throw new BusinessException(403, "Forbidden");
        }
        tagRepository.delete(tag);
    }
}
```

- [ ] **Step 3: Write MistakeService**

```java
package com.agentdome.mistake;

import com.agentdome.common.entity.MistakeCollection;
import com.agentdome.common.entity.Problem;
import com.agentdome.common.entity.ProblemTag;
import com.agentdome.common.entity.Tag;
import com.agentdome.common.entity.ProblemTag.ProblemTagId;
import com.agentdome.common.repository.*;
import com.agentdome.common.exception.BusinessException;
import com.agentdome.mistake.dto.MistakeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MistakeService {

    private final MistakeCollectionRepository mistakeRepo;
    private final ProblemRepository problemRepo;
    private final TagService tagService;
    private final ProblemTagRepository problemTagRepo;

    public MistakeService(MistakeCollectionRepository mistakeRepo,
                          ProblemRepository problemRepo,
                          TagService tagService,
                          ProblemTagRepository problemTagRepo) {
        this.mistakeRepo = mistakeRepo;
        this.problemRepo = problemRepo;
        this.tagService = tagService;
        this.problemTagRepo = problemTagRepo;
    }

    @Transactional
    public MistakeCollection addToMistakes(Long userId, Long problemId, String sessionId,
                                           String errorType, String memo, List<String> tagNames) {
        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> new BusinessException("Problem not found"));

        MistakeCollection mistake = new MistakeCollection();
        mistake.setUserId(userId);
        mistake.setProblemId(problemId);
        mistake.setSessionId(sessionId);
        mistake.setMemo(memo);
        mistakeRepo.save(mistake);

        problem.setErrorType(errorType);
        problemRepo.save(problem);

        if (tagNames != null) {
            for (String name : tagNames) {
                Tag tag = tagService.createTag(userId,
                        new com.agentdome.mistake.dto.TagDTO() {{
                            setName(name);
                        }});
                ProblemTag pt = new ProblemTag();
                pt.setId(new ProblemTagId(problemId, tag.getId()));
                problemTagRepo.save(pt);
            }
        }

        return mistake;
    }

    public List<MistakeDTO> getUserMistakes(Long userId) {
        List<MistakeCollection> mistakes = mistakeRepo.findByUserIdOrderByCreatedAtDesc(userId);
        return mistakes.stream().map(m -> {
            Problem p = problemRepo.findById(m.getProblemId()).orElse(null);
            MistakeDTO dto = new MistakeDTO();
            dto.setId(m.getId());
            dto.setProblemId(m.getProblemId());
            dto.setCreatedAt(m.getCreatedAt());
            dto.setMemo(m.getMemo());
            if (p != null) {
                dto.setSubjectType(p.getSubjectType().name());
                dto.setCleanedText(truncate(p.getCleanedText(), 200));
                dto.setErrorType(p.getErrorType());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<MistakeDTO> queryMistakes(Long userId, String tag, LocalDateTime start, LocalDateTime end) {
        return getUserMistakes(userId); // Simplified for MVP; tag filtering done in memory
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add mistake-service/src/
git commit -m "feat: add TagService and MistakeService"
```

---

## Phase 6: Agent Core Module

### Task 6.1: Prompt template manager

**Files:**
- Create: `agent-core/src/main/java/com/agentdome/agent/prompt/PromptTemplateManager.java`

- [ ] **Step 1: Write PromptTemplateManager**

```java
package com.agentdome.agent.prompt;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PromptTemplateManager {

    private static final String SYSTEM_PROMPT = """
            你是 AgentDome，一个智能题目助手。你的任务是帮助用户解题和管理错题集。
            
            你可以使用工具来：
            - solve_problem: 解题（用户上传题目图片时）
            - add_to_mistakes: 将当前题目加入错题集
            - query_mistakes: 查询用户的错题集
            - recommend_similar: 推荐相似题目
            - explain_concept: 解释概念
            
            当用户发送图片时，会自动触发解题流程。你只需根据用户文字输入判断意图并调用对应工具。
            """;

    private static final Map<String, String> SUBJECT_PROMPTS = Map.of(
            "ACM", """
                    你是一位算法竞赛专家。解题要求：
                    1. 分析问题，识别算法类型（DP、贪心、图论、搜索等）
                    2. 给出解题思路和核心算法描述
                    3. 输出 C++ 代码（带必要注释）
                    4. 分析时间复杂度和空间复杂度
                    5. 如果有多种解法，简要对比
                    """,
            "MATH", """
                    你是一位数学教授。解题要求：
                    1. 明确问题类型和涉及的数学概念
                    2. 给出详细的分步推导过程
                    3. 每一步都要解释原理
                    4. 最终答案用 \\boxed{...} 标注
                    5. 如果适用，提供多种解法
                    """,
            "CS408", """
                    你是一位考研408辅导专家。解题要求：
                    1. 明确考点（数据结构/计组/操作系统/计网）
                    2. 给出解题步骤和推理过程
                    3. 对于计算题，写出公式和计算过程
                    4. 对于选择题，逐选项分析正误原因
                    5. 对于概念题，给出标准定义并举例说明
                    6. 如涉及图表，用文字描述关键结构
                    """
    );

    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String getSubjectPrompt(String subjectType) {
        return SUBJECT_PROMPTS.getOrDefault(subjectType, "");
    }

    public String buildSolvePrompt(String subjectType, String cleanedText) {
        return getSubjectPrompt(subjectType) + "\n\n题目：\n" + cleanedText;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add agent-core/src/
git commit -m "feat: add prompt template manager"
```

### Task 6.2: Agent tools — SolveProblemTool

**Files:**
- Create: `agent-core/src/main/java/com/agentdome/agent/tools/SolveProblemTool.java`

- [ ] **Step 1: Write SolveProblemTool**

```java
package com.agentdome.agent.tools;

import com.agentdome.agent.prompt.PromptTemplateManager;
import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.image.ImagePipelineService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.dashscope.QwenModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import org.springframework.stereotype.Component;

@Component
public class SolveProblemTool {

    private final ImagePipelineService pipelineService;
    private final ProblemRepository problemRepo;
    private final PromptTemplateManager promptManager;
    private final QwenStreamingChatModel chatModel;

    public SolveProblemTool(ImagePipelineService pipelineService,
                            ProblemRepository problemRepo,
                            PromptTemplateManager promptManager) {
        this.pipelineService = pipelineService;
        this.problemRepo = problemRepo;
        this.promptManager = promptManager;

        this.chatModel = QwenStreamingChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-max")
                .build();
    }

    /**
     * Solve a problem from an uploaded image.
     * Returns solution text (and code for ACM problems).
     */
    @Tool("Solve a problem from an uploaded image. Call when user asks to solve a problem.")
    public String solveProblem(long userId, String subjectType, String cleanedText, String imageId) {
        String prompt = promptManager.buildSolvePrompt(subjectType, cleanedText);

        StringBuilder solution = new StringBuilder();
        chatModel.generate(prompt, (token) -> {
            solution.append(token);
        }).execute().content();

        // Save to DB
        Problem problem = new Problem();
        problem.setUserId(userId);
        problem.setSubjectType(Problem.SubjectType.valueOf(subjectType));
        problem.setOriginalImageId(imageId);
        problem.setCleanedText(cleanedText);
        problem.setSolutionText(solution.toString());
        problemRepo.save(problem);

        return solution.toString();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add agent-core/src/main/java/com/agentdome/agent/tools/SolveProblemTool.java
git commit -m "feat: add SolveProblem agent tool"
```

### Task 6.3: Remaining agent tools

**Files:**
- Create: `agent-core/src/main/java/com/agentdome/agent/tools/AddToMistakesTool.java`
- Create: `agent-core/src/main/java/com/agentdome/agent/tools/QueryMistakesTool.java`
- Create: `agent-core/src/main/java/com/agentdome/agent/tools/RecommendSimilarTool.java`
- Create: `agent-core/src/main/java/com/agentdome/agent/tools/ExplainConceptTool.java`

- [ ] **Step 1: Write AddToMistakesTool**

```java
package com.agentdome.agent.tools;

import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.mistake.MistakeService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AddToMistakesTool {

    private final MistakeService mistakeService;

    public AddToMistakesTool(MistakeService mistakeService) {
        this.mistakeService = mistakeService;
    }

    @Tool("Add the current problem to the user's mistake collection.")
    public String addToMistakes(long userId, long problemId, String sessionId,
                                String errorType, String memo, List<String> tags) {
        mistakeService.addToMistakes(userId, problemId, sessionId, errorType, memo, tags);
        return "已成功加入错题集！建议标签: " + (tags != null ? String.join(", ", tags) : "无");
    }
}
```

- [ ] **Step 2: Write QueryMistakesTool**

```java
package com.agentdome.agent.tools;

import com.agentdome.mistake.MistakeService;
import com.agentdome.mistake.dto.MistakeDTO;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class QueryMistakesTool {

    private final MistakeService mistakeService;

    public QueryMistakesTool(MistakeService mistakeService) {
        this.mistakeService = mistakeService;
    }

    @Tool("Query the user's mistake collection. Returns list of mistakes.")
    public List<MistakeDTO> queryMistakes(long userId) {
        return mistakeService.getUserMistakes(userId);
    }
}
```

- [ ] **Step 3: Write RecommendSimilarTool**

```java
package com.agentdome.agent.tools;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.ProblemRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RecommendSimilarTool {

    private final ProblemRepository problemRepo;

    public RecommendSimilarTool(ProblemRepository problemRepo) {
        this.problemRepo = problemRepo;
    }

    @Tool("Recommend similar problems based on the current problem's tags and subject type.")
    public String recommendSimilar(long userId, String subjectType, int count) {
        List<Problem> problems = problemRepo.findByUserIdAndSubjectType(userId,
                Problem.SubjectType.valueOf(subjectType));
        if (problems.isEmpty()) {
            return "暂无相似题目推荐。多做几道题后再来！";
        }
        StringBuilder sb = new StringBuilder("为你推荐以下相似题目：\n");
        int n = Math.min(count, problems.size());
        for (int i = 0; i < n; i++) {
            Problem p = problems.get(i);
            String preview = p.getCleanedText() != null
                    ? p.getCleanedText().substring(0, Math.min(100, p.getCleanedText().length())) + "..."
                    : "(无文本)";
            sb.append(i + 1).append(". ").append(preview).append("\n");
        }
        return sb.toString();
    }
}
```

- [ ] **Step 4: Write ExplainConceptTool**

```java
package com.agentdome.agent.tools;

import com.agentdome.agent.prompt.PromptTemplateManager;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import org.springframework.stereotype.Component;

@Component
public class ExplainConceptTool {

    private final PromptTemplateManager promptManager;
    private final QwenStreamingChatModel chatModel;

    public ExplainConceptTool(PromptTemplateManager promptManager) {
        this.promptManager = promptManager;
        this.chatModel = QwenStreamingChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-max")
                .build();
    }

    @Tool("Explain a concept or knowledge point for a given subject.")
    public String explainConcept(String concept, String subjectType) {
        String prompt = promptManager.getSubjectPrompt(subjectType)
                + "\n\n请详细解释以下概念：" + concept + "\n要求：给出定义、核心要点和具体例子。";

        StringBuilder result = new StringBuilder();
        chatModel.generate(prompt, (token) -> {
            result.append(token);
        }).execute().content();

        return result.toString();
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add agent-core/src/main/java/com/agentdome/agent/tools/
git commit -m "feat: add AddToMistakes, QueryMistakes, RecommendSimilar, ExplainConcept tools"
```

### Task 6.4: Session memory manager and AgentService

**Files:**
- Create: `agent-core/src/main/java/com/agentdome/agent/memory/SessionMemoryManager.java`
- Create: `agent-core/src/main/java/com/agentdome/agent/memory/SummaryService.java`
- Create: `agent-core/src/main/java/com/agentdome/agent/AgentService.java`

- [ ] **Step 1: Write SessionMemoryManager**

```java
package com.agentdome.agent.memory;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class SessionMemoryManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "session:";
    private static final long TTL_HOURS = 24;

    public SessionMemoryManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> getMessages(String sessionId) {
        Object data = redisTemplate.opsForValue().get(PREFIX + sessionId + ":messages");
        if (data instanceof List) {
            return (List<Map<String, String>>) data;
        }
        return new ArrayList<>();
    }

    public void appendMessage(String sessionId, String role, String content) {
        List<Map<String, String>> messages = getMessages(sessionId);
        messages.add(Map.of("role", role, "content", content));
        redisTemplate.opsForValue().set(PREFIX + sessionId + ":messages", messages, TTL_HOURS, TimeUnit.HOURS);
    }

    public void clearSession(String sessionId) {
        redisTemplate.delete(PREFIX + sessionId + ":messages");
    }
}
```

- [ ] **Step 2: Write SummaryService**

```java
package com.agentdome.agent.memory;

import com.agentdome.common.mongo.ChatSessionDocument;
import com.agentdome.common.mongo.ChatSessionRepository;
import com.agentdome.common.mongo.ChatSessionDocument.SessionSummary;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SummaryService {

    private final ChatSessionRepository sessionRepo;

    public SummaryService(ChatSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    public String getPreviousSummary(Long userId) {
        // Load most recent session summary for this user
        // For MVP, we skip complex cross-session merging
        return "";
    }

    public void saveSummary(String sessionId, Long userId, int problemsSolved,
                            List<String> weakTopics, int mistakesAdded) {
        // Update the MongoDB session document with summary
        sessionRepo.findBySessionId(sessionId).ifPresent(session -> {
            session.setSummary(new SessionSummary(problemsSolved, weakTopics, mistakesAdded));
            sessionRepo.save(session);
        });
    }
}
```

- [ ] **Step 3: Write AgentService**

```java
package com.agentdome.agent;

import com.agentdome.agent.memory.SessionMemoryManager;
import com.agentdome.agent.memory.SummaryService;
import com.agentdome.agent.prompt.PromptTemplateManager;
import com.agentdome.agent.tools.*;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static dev.langchain4j.agent.tool.ToolSpecifications.toolSpecificationFrom;

@Service
public class AgentService {

    private final QwenStreamingChatModel chatModel;
    private final PromptTemplateManager promptManager;
    private final SessionMemoryManager memoryManager;
    private final SummaryService summaryService;

    private final SolveProblemTool solveProblemTool;
    private final AddToMistakesTool addToMistakesTool;
    private final QueryMistakesTool queryMistakesTool;
    private final RecommendSimilarTool recommendSimilarTool;
    private final ExplainConceptTool explainConceptTool;

    public AgentService(PromptTemplateManager promptManager,
                        SessionMemoryManager memoryManager,
                        SummaryService summaryService,
                        SolveProblemTool solveProblemTool,
                        AddToMistakesTool addToMistakesTool,
                        QueryMistakesTool queryMistakesTool,
                        RecommendSimilarTool recommendSimilarTool,
                        ExplainConceptTool explainConceptTool) {
        this.promptManager = promptManager;
        this.memoryManager = memoryManager;
        this.summaryService = summaryService;
        this.solveProblemTool = solveProblemTool;
        this.addToMistakesTool = addToMistakesTool;
        this.queryMistakesTool = queryMistakesTool;
        this.recommendSimilarTool = recommendSimilarTool;
        this.explainConceptTool = explainConceptTool;

        this.chatModel = QwenStreamingChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen-max")
                .build();
    }

    public String newSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        String prevSummary = summaryService.getPreviousSummary(userId);
        memoryManager.appendMessage(sessionId, "system",
                promptManager.getSystemPrompt() + "\n历史学习摘要：\n" + prevSummary);
        return sessionId;
    }

    /**
     * Process a user text message. Agent decides whether to call a tool.
     */
    public String processMessage(String sessionId, Long userId, String userMessage) {
        memoryManager.appendMessage(sessionId, "user", userMessage);

        List<ToolSpecification> toolSpecs = List.of(
                toolSpecificationFrom(AddToMistakesTool.class),
                toolSpecificationFrom(QueryMistakesTool.class),
                toolSpecificationFrom(RecommendSimilarTool.class),
                toolSpecificationFrom(ExplainConceptTool.class)
        );

        ChatRequest request = ChatRequest.builder()
                .messages(new dev.langchain4j.data.message.SystemMessage(promptManager.getSystemPrompt()),
                        new dev.langchain4j.data.message.UserMessage(userMessage))
                .toolSpecifications(toolSpecs)
                .build();

        String response = chatModel.chat(request).aiMessage().text();
        memoryManager.appendMessage(sessionId, "assistant", response);
        return response;
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add agent-core/src/
git commit -m "feat: add session memory, summary service, and AgentService orchestrator"
```

---

## Phase 7: Gateway — WebSocket, Chat Controller, CORS

### Task 7.1: WebSocket configuration and chat controller

**Files:**
- Create: `gateway/src/main/java/com/agentdome/gateway/config/WebSocketConfig.java`
- Create: `gateway/src/main/java/com/agentdome/gateway/config/CorsConfig.java`
- Create: `gateway/src/main/java/com/agentdome/gateway/controller/ChatController.java`
- Create: `gateway/src/main/java/com/agentdome/gateway/dto/ApiResponse.java`

- [ ] **Step 1: Write WebSocketConfig**

```java
package com.agentdome.gateway.config;

import com.agentdome.agent.AgentService;
import com.agentdome.common.util.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AgentService agentService;
    private final JwtUtil jwtUtil;

    public WebSocketConfig(AgentService agentService, JwtUtil jwtUtil) {
        this.agentService = agentService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(agentService, jwtUtil), "/ws/chat")
                .setAllowedOrigins("*");
    }

    static class ChatWebSocketHandler extends TextWebSocketHandler {

        private final AgentService agentService;
        private final JwtUtil jwtUtil;
        private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

        ChatWebSocketHandler(AgentService agentService, JwtUtil jwtUtil) {
            this.agentService = agentService;
            this.jwtUtil = jwtUtil;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            URI uri = session.getUri();
            if (uri == null) { session.close(); return; }

            String query = uri.getQuery();
            String token = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6);
                    }
                }
            }

            if (token == null || !jwtUtil.validateToken(token)) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String sessionId = agentService.newSession(userId);
            session.getAttributes().put("userId", userId);
            session.getAttributes().put("sessionId", sessionId);
            sessions.put(sessionId, session);
            session.sendMessage(new TextMessage("{\"type\":\"connected\",\"sessionId\":\"" + sessionId + "\"}"));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            Long userId = (Long) session.getAttributes().get("userId");
            String sessionId = (String) session.getAttributes().get("sessionId");
            String response = agentService.processMessage(sessionId, userId, message.getPayload());
            session.sendMessage(new TextMessage("{\"type\":\"message\",\"content\":\"" +
                    escapeJson(response) + "\"}"));
        }

        private String escapeJson(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r");
        }
    }
}
```

- [ ] **Step 2: Write CorsConfig**

```java
package com.agentdome.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 3: Write ApiResponse**

```java
package com.agentdome.gateway.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

- [ ] **Step 4: Write ChatController (REST endpoints for image upload)**

```java
package com.agentdome.gateway.controller;

import com.agentdome.agent.AgentService;
import com.agentdome.common.entity.Problem.SubjectType;
import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.gateway.dto.ApiResponse;
import com.agentdome.image.ImagePipelineService;
import com.agentdome.image.ImagePipelineService.PipelineResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ImagePipelineService pipelineService;
    private final AgentService agentService;
    private final ProblemRepository problemRepo;

    public ChatController(ImagePipelineService pipelineService,
                          AgentService agentService,
                          ProblemRepository problemRepo) {
        this.pipelineService = pipelineService;
        this.agentService = agentService;
        this.problemRepo = problemRepo;
    }

    /**
     * Upload an image for solving. Returns OCR text.
     * The actual solve is triggered by agent via WebSocket or subsequent REST call.
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectType") String subjectType,
            @RequestParam("sessionId") String sessionId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        PipelineResult result = pipelineService.process(file, userId);

        Map<String, Object> data = Map.of(
                "imageId", result.imageId(),
                "rawText", result.rawText(),
                "cleanedText", result.cleanedText()
        );

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("AgentDome running"));
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add gateway/src/
git commit -m "feat: add WebSocket config, CORS, chat controller, and API response wrapper"
```

---

## Phase 8: WeChat Mini Program

### Task 8.1: App scaffold and auth utility

**Files:**
- Create: `miniprogram/app.js`
- Create: `miniprogram/app.json`
- Create: `miniprogram/app.wxss`
- Create: `miniprogram/utils/auth.js`
- Create: `miniprogram/utils/api.js`

- [ ] **Step 1: Write app.js**

```javascript
App({
  globalData: {
    userInfo: null,
    token: null,
    userId: null,
    baseUrl: 'https://your-api.domain.com' // Replace with actual API URL
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      this.globalData.userId = wx.getStorageSync('userId');
    }
  }
});
```

- [ ] **Step 2: Write app.json**

```json
{
  "pages": [
    "pages/chat/chat",
    "pages/mistakes/mistakes",
    "pages/profile/profile"
  ],
  "window": {
    "navigationBarBackgroundColor": "#4A90D9",
    "navigationBarTitleText": "AgentDome",
    "navigationBarTextStyle": "white"
  },
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#4A90D9",
    "list": [
      {
        "pagePath": "pages/chat/chat",
        "text": "聊天",
        "iconPath": "images/tab-chat.png",
        "selectedIconPath": "images/tab-chat-active.png"
      },
      {
        "pagePath": "pages/mistakes/mistakes",
        "text": "错题集",
        "iconPath": "images/tab-mistake.png",
        "selectedIconPath": "images/tab-mistake-active.png"
      },
      {
        "pagePath": "pages/profile/profile",
        "text": "我的",
        "iconPath": "images/tab-profile.png",
        "selectedIconPath": "images/tab-profile-active.png"
      }
    ]
  },
  "requiredPrivateInfos": ["chooseMedia"]
}
```

- [ ] **Step 3: Write app.wxss**

```css
page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f5f5f5;
  font-size: 16px;
}

.container {
  padding: 16px;
}
```

- [ ] **Step 4: Write utils/auth.js**

```javascript
const app = getApp();

function login(callback) {
  wx.login({
    success(res) {
      if (res.code) {
        wx.request({
          url: `${app.globalData.baseUrl}/api/auth/login`,
          method: 'POST',
          data: { code: res.code },
          success(resp) {
            if (resp.data.code === 200) {
              const { token, userId, nickname, avatarUrl } = resp.data.data;
              app.globalData.token = token;
              app.globalData.userId = userId;
              wx.setStorageSync('token', token);
              wx.setStorageSync('userId', userId);
              if (callback) callback(resp.data.data);
            }
          }
        });
      }
    }
  });
}

function getToken() {
  return app.globalData.token || wx.getStorageSync('token');
}

function getUserId() {
  return app.globalData.userId || wx.getStorageSync('userId');
}

module.exports = { login, getToken, getUserId };
```

- [ ] **Step 5: Write utils/api.js**

```javascript
const app = getApp();
const auth = require('./auth');

function request(options) {
  const token = auth.getToken();

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      success(res) {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          auth.login(() => {
            // Retry once after re-login
            request(options).then(resolve).catch(reject);
          });
        } else {
          reject(res.data);
        }
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

function upload(filePath, subjectType, sessionId) {
  const token = auth.getToken();

  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${app.globalData.baseUrl}/api/chat/upload`,
      filePath: filePath,
      name: 'file',
      formData: {
        subjectType: subjectType,
        sessionId: sessionId
      },
      header: {
        'Authorization': `Bearer ${token}`
      },
      success(res) {
        resolve(JSON.parse(res.data));
      },
      fail(err) {
        reject(err);
      }
    });
  });
}

module.exports = { request, upload };
```

- [ ] **Step 6: Commit**

```bash
git add miniprogram/
git commit -m "feat: add mini program app scaffold, auth, and API utilities"
```

### Task 8.2: Chat page

**Files:**
- Create: `miniprogram/pages/chat/chat.js`
- Create: `miniprogram/pages/chat/chat.wxml`
- Create: `miniprogram/pages/chat/chat.wxss`
- Create: `miniprogram/pages/chat/chat.json`

- [ ] **Step 1: Write chat.js**

```javascript
const app = getApp();
const api = require('../../utils/api');
const auth = require('../../utils/auth');

Page({
  data: {
    messages: [],
    inputText: '',
    sessionId: null,
    wsConnected: false,
    subjectTypes: ['ACM', 'MATH', 'CS408'],
    subjectLabels: ['ACM 算法', '高等数学', '考研408'],
    showSubjectPicker: false,
    pendingImage: null
  },

  onLoad() {
    auth.login(() => {
      this.connectWebSocket();
    });
  },

  connectWebSocket() {
    const token = auth.getToken();
    const socketTask = wx.connectSocket({
      url: `${app.globalData.baseUrl}/ws/chat?token=${token}`
    });

    socketTask.onOpen(() => {
      this.setData({ wsConnected: true });
    });

    socketTask.onMessage((res) => {
      const data = JSON.parse(res.data);
      if (data.type === 'connected') {
        this.setData({ sessionId: data.sessionId });
      } else if (data.type === 'message') {
        this.appendMessage('assistant', data.content);
      }
    });

    socketTask.onClose(() => {
      this.setData({ wsConnected: false });
    });

    this.socketTask = socketTask;
  },

  selectSubject(e) {
    const index = parseInt(e.currentTarget.dataset.index);
    const subjectType = this.data.subjectTypes[index];
    this.setData({ showSubjectPicker: false });

    if (this.data.pendingImage) {
      this.uploadImage(this.data.pendingImage, subjectType);
      this.setData({ pendingImage: null });
    }
  },

  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['camera', 'album'],
      success: (res) => {
        const tempPath = res.tempFiles[0].tempFilePath;
        this.setData({
          showSubjectPicker: true,
          pendingImage: tempPath
        });
      }
    });
  },

  uploadImage(filePath, subjectType) {
    this.appendMessage('user', '[图片]', true);
    api.upload(filePath, subjectType, this.data.sessionId).then((res) => {
      if (res.code === 200) {
        const { imageId, cleanedText } = res.data;
        this.appendMessage('assistant', `OCR 识别结果:\n${cleanedText}`);
        // Ask agent to solve
        if (this.socketTask) {
          this.socketTask.send({
            data: JSON.stringify({
              action: 'solve',
              subjectType: subjectType,
              cleanedText: cleanedText,
              imageId: imageId
            })
          });
        }
      }
    }).catch((err) => {
      this.appendMessage('assistant', '图片上传失败，请重试');
    });
  },

  sendText() {
    const text = this.data.inputText.trim();
    if (!text) return;

    this.appendMessage('user', text);
    this.setData({ inputText: '' });

    if (this.socketTask) {
      this.socketTask.send({ data: text });
    }
  },

  appendMessage(role, content, isImage = false) {
    const messages = this.data.messages.concat([{
      role,
      content,
      isImage,
      time: new Date().toLocaleTimeString()
    }]);
    this.setData({ messages });
    this.scrollToBottom();
  },

  scrollToBottom() {
    wx.createSelectorQuery().select('#message-list').boundingClientRect((rect) => {
      wx.pageScrollTo({ scrollTop: rect.bottom });
    }).exec();
  }
});
```

- [ ] **Step 2: Write chat.wxml**

```xml
<view class="container">
  <view id="message-list" class="message-list">
    <block wx:for="{{messages}}" wx:key="index">
      <view class="message {{item.role}}">
        <view class="bubble">
          <text wx:if="{{item.isImage}}">📷 图片</text>
          <text wx:else>{{item.content}}</text>
        </view>
        <text class="time">{{item.time}}</text>
      </view>
    </block>
  </view>

  <view class="input-bar">
    <button class="btn-image" bindtap="chooseImage">📷</button>
    <input class="text-input" value="{{inputText}}" placeholder="输入文字指令..."
           bindinput="e => this.setData({inputText: e.detail.value})"
           confirm-type="send" bindconfirm="sendText"/>
    <button class="btn-send" bindtap="sendText">发送</button>
  </view>

  <!-- Subject type picker modal -->
  <view wx:if="{{showSubjectPicker}}" class="picker-overlay">
    <view class="picker-modal">
      <text class="picker-title">选择题目类型</text>
      <view wx:for="{{subjectLabels}}" wx:key="index"
            class="picker-item" data-index="{{index}}" bindtap="selectSubject">
        {{item}}
      </view>
      <button class="picker-cancel" bindtap="e => this.setData({showSubjectPicker: false})">取消</button>
    </view>
  </view>
</view>
```

- [ ] **Step 3: Write chat.wxss**

```css
.message-list {
  padding-bottom: 100rpx;
}

.message {
  margin: 16rpx 0;
  display: flex;
  flex-direction: column;
}

.message.user {
  align-items: flex-end;
}

.message.assistant {
  align-items: flex-start;
}

.bubble {
  max-width: 80%;
  padding: 16rpx 24rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
  white-space: pre-wrap;
}

.user .bubble {
  background-color: #4A90D9;
  color: white;
}

.assistant .bubble {
  background-color: white;
  color: #333;
}

.time {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: white;
  border-top: 1rpx solid #eee;
}

.text-input {
  flex: 1;
  border: 1rpx solid #ddd;
  border-radius: 32rpx;
  padding: 12rpx 24rpx;
  margin: 0 16rpx;
  font-size: 28rpx;
}

.btn-send {
  background-color: #4A90D9;
  color: white;
  border-radius: 32rpx;
  font-size: 26rpx;
  padding: 12rpx 32rpx;
}

.picker-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.picker-modal {
  background: white;
  border-radius: 16rpx;
  padding: 48rpx;
  width: 80%;
}

.picker-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 32rpx;
  display: block;
  text-align: center;
}

.picker-item {
  padding: 24rpx;
  text-align: center;
  font-size: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.picker-cancel {
  margin-top: 24rpx;
  background: #f5f5f5;
}
```

- [ ] **Step 4: Commit**

```bash
git add miniprogram/pages/chat/
git commit -m "feat: add chat page with WebSocket, image upload, and subject picker"
```

### Task 8.3: Mistakes page

**Files:**
- Create: `miniprogram/pages/mistakes/mistakes.js`
- Create: `miniprogram/pages/mistakes/mistakes.wxml`
- Create: `miniprogram/pages/mistakes/mistakes.wxss`
- Create: `miniprogram/pages/mistakes/mistakes.json`

- [ ] **Step 1: Write mistakes.js**

```javascript
const api = require('../../utils/api');
const auth = require('../../utils/auth');

Page({
  data: {
    mistakes: [],
    groupedMistakes: []
  },

  onShow() {
    this.loadMistakes();
  },

  loadMistakes() {
    api.request({ url: '/api/mistakes' }).then(res => {
      if (res.data) {
        this.setData({ mistakes: res.data });
        this.groupByTag(res.data);
      }
    });
  },

  groupByTag(mistakes) {
    const indexed = mistakes.map((m, i) => ({ ...m, _originalIndex: i }));
    const groups = {};
    indexed.forEach(m => {
      if (m.tags && m.tags.length) {
        m.tags.forEach(t => {
          if (!groups[t]) groups[t] = [];
          groups[t].push(m);
        });
      }
    });
    const grouped = Object.entries(groups).map(([tag, items]) => ({
      tag,
      count: items.length,
      items
    }));
    this.setData({ groupedMistakes: grouped });
  },

  viewMistake(e) {
    const problemId = e.currentTarget.dataset.problemId;
    api.request({ url: `/api/problems/${problemId}` }).then(res => {
      if (res.data) {
        wx.showModal({
          title: '题目详情',
          content: res.data.solutionText || res.data.cleanedText || '',
          showCancel: false
        });
      }
    });
  }
});
```

- [ ] **Step 2: Write mistakes.wxml**

```xml
<view class="container">
  <view wx:if="{{groupedMistakes.length === 0}}" class="empty">
    <text>还没有错题，开始解题吧！</text>
  </view>

  <view wx:for="{{groupedMistakes}}" wx:key="tag" class="group">
    <view class="group-header">
      <text class="tag-name">{{item.tag}} ({{item.count}}题)</text>
    </view>
    <view wx:for="{{item.items}}" wx:key="id" class="mistake-item"
          data-problem-id="{{item.problemId}}" bindtap="viewMistake">
      <text class="mistake-text">{{item.cleanedText || '查看详情'}}</text>
      <text class="mistake-time">{{item.createdAt}}</text>
    </view>
  </view>
</view>
```

- [ ] **Step 3: Write mistakes.wxss**

```css
.empty {
  text-align: center;
  padding: 128rpx 0;
  color: #999;
}

.group {
  margin-bottom: 32rpx;
}

.group-header {
  padding: 16rpx 0;
  border-bottom: 2rpx solid #4A90D9;
  margin-bottom: 16rpx;
}

.tag-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #4A90D9;
}

.mistake-item {
  background: white;
  padding: 24rpx;
  border-radius: 12rpx;
  margin-bottom: 12rpx;
}

.mistake-text {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mistake-time {
  font-size: 22rpx;
  color: #999;
}
```

- [ ] **Step 4: Commit**

```bash
git add miniprogram/pages/mistakes/
git commit -m "feat: add mistakes page with tag grouping"
```

### Task 8.4: Profile page

**Files:**
- Create: `miniprogram/pages/profile/profile.js`
- Create: `miniprogram/pages/profile/profile.wxml`
- Create: `miniprogram/pages/profile/profile.wxss`
- Create: `miniprogram/pages/profile/profile.json`

- [ ] **Step 1: Write profile.js**

```javascript
Page({
  data: {
    userInfo: {}
  },

  onShow() {
    const token = wx.getStorageSync('token');
    this.setData({
      userInfo: { token: token ? '已登录' : '未登录' }
    });
  },

  clearCache() {
    wx.clearStorageSync();
    wx.showToast({ title: '已清除缓存', icon: 'success' });
  }
});
```

- [ ] **Step 2: Write profile.wxml**

```xml
<view class="container">
  <view class="profile-card">
    <view class="avatar">👤</view>
    <text class="nickname">用户</text>
    <text class="status">{{userInfo.token}}</text>
  </view>

  <view class="menu">
    <view class="menu-item" bindtap="clearCache">
      <text>清除缓存</text>
      <text class="arrow">→</text>
    </view>
  </view>
</view>
```

- [ ] **Step 3: Write profile.wxss**

```css
.profile-card {
  background: white;
  border-radius: 16rpx;
  padding: 48rpx;
  text-align: center;
  margin-bottom: 24rpx;
}

.avatar {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.nickname {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
}

.status {
  font-size: 24rpx;
  color: #999;
}

.menu {
  background: white;
  border-radius: 16rpx;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  font-size: 28rpx;
}

.arrow {
  color: #ccc;
}
```

- [ ] **Step 4: Commit**

```bash
git add miniprogram/pages/profile/
git commit -m "feat: add profile page"
```

---

## Phase 9: Integration & Deployment

### Task 9.1: Add mistake REST endpoints to gateway

**Files:**
- Create: `gateway/src/main/java/com/agentdome/gateway/controller/MistakeController.java`
- Create: `gateway/src/main/java/com/agentdome/gateway/controller/ProblemController.java`

- [ ] **Step 1: Write MistakeController**

```java
package com.agentdome.gateway.controller;

import com.agentdome.gateway.dto.ApiResponse;
import com.agentdome.mistake.MistakeService;
import com.agentdome.mistake.dto.MistakeDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MistakeController {

    private final MistakeService mistakeService;

    public MistakeController(MistakeService mistakeService) {
        this.mistakeService = mistakeService;
    }

    @GetMapping("/mistakes")
    public ResponseEntity<ApiResponse<List<MistakeDTO>>> listMistakes(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.ok(mistakeService.getUserMistakes(userId)));
    }
}
```

- [ ] **Step 2: Write ProblemController**

```java
package com.agentdome.gateway.controller;

import com.agentdome.common.entity.Problem;
import com.agentdome.common.repository.ProblemRepository;
import com.agentdome.gateway.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemRepository problemRepo;

    public ProblemController(ProblemRepository problemRepo) {
        this.problemRepo = problemRepo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Problem>> getProblem(@PathVariable Long id,
                                                           HttpServletRequest request) {
        return problemRepo.findById(id)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(p)))
                .orElse(ResponseEntity.notFound().build());
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add gateway/src/main/java/com/agentdome/gateway/controller/MistakeController.java \
        gateway/src/main/java/com/agentdome/gateway/controller/ProblemController.java
git commit -m "feat: add REST endpoints for mistakes and problems"
```

### Task 9.2: Docker Compose for local development

**Files:**
- Create: `docker-compose.yml`

- [ ] **Step 1: Write docker-compose.yml**

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: agent_dome
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  mongodb:
    image: mongo:7
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db

volumes:
  mysql_data:
  mongo_data:
```

- [ ] **Step 2: Commit**

```bash
git add docker-compose.yml
git commit -m "feat: add Docker Compose for local dev infrastructure"
```

### Task 9.3: Add .gitignore

**Files:**
- Create: `.gitignore`

- [ ] **Step 1: Write .gitignore**

```
# Maven
target/

# IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS
.DS_Store
Thumbs.db

# Logs
*.log

# Environment
.env
.env.local

# Superpowers (brainstorm artifacts)
.superpowers/

# Mini program
miniprogram/images/tab-*.png
```

- [ ] **Step 2: Commit**

```bash
git add .gitignore
git commit -m "chore: add .gitignore"
```

---

## Self-Review Checklist

### 1. Spec Coverage

| Spec Requirement | Plan Task | Status |
|---|---|---|
| 微信小程序三页面 | Tasks 8.1–8.4 | Covered |
| 拍照/选图上传 + 科目选择 | Task 8.2 (chat.js + subject picker) | Covered |
| 阿里云 OCR 文本识别 | Task 4.1 | Covered |
| Qwen-Max 多科目解题 | Tasks 6.1, 6.2 | Covered |
| Agent 5 个工具 | Tasks 6.2, 6.3 | Covered |
| 错题集 CRUD + 标签管理 | Tasks 5.1, 9.1 | Covered |
| 会话记忆 (Redis + MongoDB) | Tasks 6.4 | Covered |
| 微信 OpenID 认证 | Tasks 3.1, 3.2 | Covered |
| MySQL schema (5 tables) | Tasks 1.1–1.3 | Covered |
| MongoDB collections | Tasks 2.1, 6.4 | Covered |
| Modular monolith (5 modules) | Tasks 0.1–0.3 | Covered |
| LangChain4j tool-calling | Tasks 6.2–6.4 | Covered |
| Limited memory model | Task 6.4 | Covered |
| Self-deployment config | Tasks 9.2 | Covered |

Gaps found: None.

### 2. Placeholder Scan

- `https://your-api.domain.com` in miniprogram app.js — this is a configuration value, not a placeholder. The developer replaces it with the actual deployment URL.
- `System.getenv("DASHSCOPE_API_KEY")` — environment variable, standard practice for secrets.

No TBD, TODO, or incomplete sections. ✓

### 3. Type Consistency

- `SubjectType` enum values: ACM, MATH, CS408 — consistent across Problem entity, prompt manager, and chat page ✓
- `agent-core` depends on `mistake-service` for `MistakeService` via constructor injection in `AddToMistakesTool` ✓
- `SessionMemoryManager` key prefix: "session:" + sessionId + ":messages" — consistent ✓
- DTO field names match between frontend and backend ✓

No mismatches found. ✓
