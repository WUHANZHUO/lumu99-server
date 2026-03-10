# Lumu99 Forum Backend Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build production-ready backend APIs for Lumu99 forum/community with admin governance, post review center, and complete API documentation.

**Architecture:** Use modular monolith in `lumu99-server` with Spring Boot layered structure (`controller -> service -> repository`) and MySQL as the only persistence for V1. Implement domain modules incrementally with strict TDD and frequent commits.

**Tech Stack:** Java 17, Spring Boot 3, Spring Security, JWT, MyBatis-Plus, Flyway, MySQL, springdoc-openapi, JUnit 5, MockMvc, Testcontainers (MySQL)

---

### Task 1: Project Baseline and Dependency Setup

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.yml`
- Create: `src/main/resources/application-dev.yml`
- Create: `src/main/resources/application-test.yml`
- Test: `src/test/java/com/lumu99/forum/bootstrap/ApplicationBootstrapTest.java`

**Step 1: Write the failing test**

```java
@SpringBootTest
class ApplicationBootstrapTest {
    @Test
    void contextLoads() {}
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ApplicationBootstrapTest test`
Expected: FAIL due to package/class mismatch before refactor.

**Step 3: Write minimal implementation**

- Refactor package root from `com.lumu99.demo` to `com.lumu99.forum`.
- Add dependencies: `validation`, `security`, `mybatis-plus-boot-starter`, `mysql-connector-j`, `flyway-core`, `jjwt-api/impl/jackson`, `springdoc-openapi-starter-webmvc-ui`, `spring-boot-starter-test`, `testcontainers-mysql`.
- Add profile-based datasource placeholders in `application-dev.yml` and `application-test.yml`.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ApplicationBootstrapTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add pom.xml src/main/resources/application.yml src/main/resources/application-dev.yml src/main/resources/application-test.yml src/test/java/com/lumu99/forum/bootstrap/ApplicationBootstrapTest.java
git commit -m "chore: bootstrap forum backend project structure"
```

### Task 2: Unified Error Response + Request ID Logging Filter

**Files:**
- Create: `src/main/java/com/lumu99/forum/common/api/ApiError.java`
- Create: `src/main/java/com/lumu99/forum/common/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/com/lumu99/forum/common/web/RequestIdFilter.java`
- Test: `src/test/java/com/lumu99/forum/common/GlobalExceptionHandlerTest.java`

**Step 1: Write the failing test**

```java
@WebMvcTest(controllers = DummyController.class)
class GlobalExceptionHandlerTest {
    @Autowired MockMvc mvc;

    @Test
    void shouldReturnUnifiedErrorShape() throws Exception {
        mvc.perform(get("/dummy/error"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("REQ_400_BAD_REQUEST"))
           .andExpect(jsonPath("$.message").exists())
           .andExpect(jsonPath("$.requestId").exists());
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=GlobalExceptionHandlerTest test`
Expected: FAIL because handler/filter not implemented.

**Step 3: Write minimal implementation**

- Implement `ApiError { code, message, requestId }`.
- Add `@RestControllerAdvice` to map validation/security/business exceptions.
- Add servlet filter to create/propagate `X-Request-Id` and put into MDC.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=GlobalExceptionHandlerTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/common src/test/java/com/lumu99/forum/common/GlobalExceptionHandlerTest.java
git commit -m "feat: add unified error response and request id filter"
```

### Task 3: Database Schema Migration (V1)

**Files:**
- Create: `src/main/resources/db/migration/V1__init_forum_schema.sql`
- Test: `src/test/java/com/lumu99/forum/db/FlywaySchemaTest.java`

**Step 1: Write the failing test**

```java
@SpringBootTest
class FlywaySchemaTest {
    @Autowired JdbcTemplate jdbc;

    @Test
    void shouldCreateCoreTables() {
        Integer count = jdbc.queryForObject(
            "select count(*) from information_schema.tables where table_name in ('users','forum_posts','audit_logs')",
            Integer.class);
        assertThat(count).isGreaterThanOrEqualTo(3);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=FlywaySchemaTest test`
Expected: FAIL before migration exists.

**Step 3: Write minimal implementation**

- Create all approved tables:
  - `users`, `admin_settings`, `forum_tags`, `forbidden_words`
  - `quiz_questions`, `quiz_options`, `quiz_config`, `invite_codes`
  - `forum_posts`, `forum_post_tag_rel`, `forum_comments`
  - `content_story`, `content_timeline`, `content_photo`, `content_video`, `content_world`, `content_event`
  - `votes`, `dm_threads`, `dm_messages`, `audit_logs`
- Add unique constraints for `username`, `weibo_name`, `user_uuid`, vote uniqueness.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=FlywaySchemaTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/resources/db/migration/V1__init_forum_schema.sql src/test/java/com/lumu99/forum/db/FlywaySchemaTest.java
git commit -m "feat: add initial schema migration for forum backend"
```

### Task 4: Security Foundation (JWT + Role Guards)

**Files:**
- Create: `src/main/java/com/lumu99/forum/auth/security/JwtService.java`
- Create: `src/main/java/com/lumu99/forum/auth/security/JwtAuthFilter.java`
- Create: `src/main/java/com/lumu99/forum/config/SecurityConfig.java`
- Test: `src/test/java/com/lumu99/forum/auth/SecurityAccessTest.java`

**Step 1: Write the failing test**

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {
    @Autowired MockMvc mvc;

    @Test
    void guestCannotAccessAdminPath() throws Exception {
        mvc.perform(get("/admin/settings"))
           .andExpect(status().isUnauthorized());
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=SecurityAccessTest test`
Expected: FAIL because security rules missing.

**Step 3: Write minimal implementation**

- Stateless JWT authentication filter.
- Security rules:
  - `/auth/**` permitAll
  - `/admin/**` ADMIN only
  - `/forum/**` authenticated
  - read-only content endpoints open by visibility rules (handled in service)

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=SecurityAccessTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/auth/security src/main/java/com/lumu99/forum/config/SecurityConfig.java src/test/java/com/lumu99/forum/auth/SecurityAccessTest.java
git commit -m "feat: add jwt security foundation and admin route guards"
```

### Task 5: Auth + User Lifecycle APIs

**Files:**
- Create: `src/main/java/com/lumu99/forum/auth/controller/AuthController.java`
- Create: `src/main/java/com/lumu99/forum/user/controller/UserController.java`
- Create: `src/main/java/com/lumu99/forum/user/service/UserLifecycleService.java`
- Create: `src/main/java/com/lumu99/forum/user/repository/*`
- Test: `src/test/java/com/lumu99/forum/auth/AuthLifecycleTest.java`

**Step 1: Write the failing test**

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthLifecycleTest {
    @Autowired MockMvc mvc;

    @Test
    void bannedUserCannotLogin() throws Exception {
        // seed banned user
        mvc.perform(post("/auth/login")
           .contentType(MediaType.APPLICATION_JSON)
           .content("{\"username\":\"u1\",\"password\":\"p1\"}"))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.code").value("LOGIN_403_ACCOUNT_BANNED"));
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=AuthLifecycleTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Implement login, logout endpoint.
- Implement user self operations:
  - `PUT /users/me/username`
  - `PUT /users/me/password`
  - `DELETE /users/me` (logical)
- Generate UUID for each user account on creation.
- Ensure uniqueness checks for `username`, `weibo_name`.
- Mask password values from logs/audit.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=AuthLifecycleTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/auth src/main/java/com/lumu99/forum/user src/test/java/com/lumu99/forum/auth/AuthLifecycleTest.java
git commit -m "feat: implement auth and user self lifecycle APIs"
```

### Task 6: Quiz Registration Flow

**Files:**
- Create: `src/main/java/com/lumu99/forum/auth/controller/QuizRegistrationController.java`
- Create: `src/main/java/com/lumu99/forum/quiz/service/QuizService.java`
- Create: `src/main/java/com/lumu99/forum/quiz/repository/*`
- Test: `src/test/java/com/lumu99/forum/auth/QuizRegistrationTest.java`

**Step 1: Write the failing test**

```java
@Test
void fillBlankMustMatchExactly() throws Exception {
    mvc.perform(post("/auth/register/quiz/submit")
       .contentType(MediaType.APPLICATION_JSON)
       .content("{...wrong_case_or_spacing...}"))
       .andExpect(status().isUnprocessableEntity())
       .andExpect(jsonPath("$.code").value("REG_422_QUIZ_ANSWER_WRONG"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=QuizRegistrationTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- `POST /auth/register/quiz/start`: random pull by configured count.
- `POST /auth/register/quiz/submit`: verify answers, create user, return token.
- Fill-blank check uses exact string match.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=QuizRegistrationTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/quiz src/main/java/com/lumu99/forum/auth/controller/QuizRegistrationController.java src/test/java/com/lumu99/forum/auth/QuizRegistrationTest.java
git commit -m "feat: add quiz-based registration workflow"
```

### Task 7: Invite-code Registration + Admin Invite Management

**Files:**
- Create: `src/main/java/com/lumu99/forum/invite/controller/InviteRegistrationController.java`
- Create: `src/main/java/com/lumu99/forum/admin/controller/AdminInviteController.java`
- Create: `src/main/java/com/lumu99/forum/invite/service/InviteCodeService.java`
- Test: `src/test/java/com/lumu99/forum/invite/InviteCodeFlowTest.java`

**Step 1: Write the failing test**

```java
@Test
void usedInviteCodeCannotBeReused() throws Exception {
    mvc.perform(post("/auth/register/invite").contentType(APPLICATION_JSON).content("{...}") )
       .andExpect(status().isCreated());

    mvc.perform(post("/auth/register/invite").contentType(APPLICATION_JSON).content("{same_code...}") )
       .andExpect(status().isUnprocessableEntity())
       .andExpect(jsonPath("$.code").value("REG_422_INVITE_CODE_INVALID"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=InviteCodeFlowTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Register by invite code bypasses quiz.
- Admin endpoints to create/list invite codes.
- State transition support: `UNUSED -> USED/EXPIRED/DISABLED`.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=InviteCodeFlowTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/invite src/main/java/com/lumu99/forum/admin/controller/AdminInviteController.java src/test/java/com/lumu99/forum/invite/InviteCodeFlowTest.java
git commit -m "feat: add invite-code registration and admin invite management"
```

### Task 8: Admin Config (Visibility, Review Switch, DM Switch) + Forbidden Words + Tags + Quiz Admin

**Files:**
- Create: `src/main/java/com/lumu99/forum/admin/controller/AdminSettingsController.java`
- Create: `src/main/java/com/lumu99/forum/admin/controller/AdminForbiddenWordController.java`
- Create: `src/main/java/com/lumu99/forum/admin/controller/AdminForumTagController.java`
- Create: `src/main/java/com/lumu99/forum/admin/controller/AdminQuizController.java`
- Test: `src/test/java/com/lumu99/forum/admin/AdminConfigTest.java`

**Step 1: Write the failing test**

```java
@Test
void nonAdminCannotUpdateSettings() throws Exception {
    mvc.perform(put("/admin/settings").header("Authorization", userToken).contentType(APPLICATION_JSON).content("{...}"))
       .andExpect(status().isForbidden());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=AdminConfigTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Global settings update/read.
- Forbidden words CRUD.
- Forum tags CRUD with `admin_only` flag.
- Quiz question/config CRUD.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=AdminConfigTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/admin src/test/java/com/lumu99/forum/admin/AdminConfigTest.java
git commit -m "feat: add admin governance for settings words tags and quiz"
```

### Task 9: Forum Post + Review Trigger + Pin/Unpin

**Files:**
- Create: `src/main/java/com/lumu99/forum/forum/controller/ForumPostController.java`
- Create: `src/main/java/com/lumu99/forum/forum/service/ForumPostService.java`
- Create: `src/main/java/com/lumu99/forum/review/service/ReviewDecisionEngine.java`
- Test: `src/test/java/com/lumu99/forum/forum/ForumPostReviewTriggerTest.java`

**Step 1: Write the failing test**

```java
@Test
void forbiddenWordPostMustBePending() throws Exception {
    mvc.perform(post("/forum/posts").header("Authorization", userToken).contentType(APPLICATION_JSON).content("{\"title\":\"...\",\"content\":\"contains_word\"}"))
       .andExpect(status().isCreated())
       .andExpect(jsonPath("$.data.reviewStatus").value("PENDING"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ForumPostReviewTriggerTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Create/list/update/delete forum posts.
- Apply review triggers:
  - forbidden word -> pending (mandatory)
  - user post review switch -> pending
- Add admin pin/unpin endpoint.
- Enforce tag policy (`admin_only` forbidden for normal users).

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ForumPostReviewTriggerTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/forum src/main/java/com/lumu99/forum/review src/test/java/com/lumu99/forum/forum/ForumPostReviewTriggerTest.java
git commit -m "feat: implement forum post flow with review triggers and pin"
```

### Task 10: Review Center APIs (Approve/Reject)

**Files:**
- Create: `src/main/java/com/lumu99/forum/review/controller/AdminReviewController.java`
- Create: `src/main/java/com/lumu99/forum/review/service/ReviewService.java`
- Test: `src/test/java/com/lumu99/forum/review/AdminReviewFlowTest.java`

**Step 1: Write the failing test**

```java
@Test
void adminCanApprovePendingPost() throws Exception {
    mvc.perform(post("/admin/reviews/posts/{id}/approve", pendingPostId)
        .header("Authorization", adminToken))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=AdminReviewFlowTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Pending list endpoint.
- Approve/reject endpoint.
- Reject reason support.
- Enforce state transition validity.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=AdminReviewFlowTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/review/controller src/main/java/com/lumu99/forum/review/service/ReviewService.java src/test/java/com/lumu99/forum/review/AdminReviewFlowTest.java
git commit -m "feat: add admin review center approve reject workflow"
```

### Task 11: Forum Comments + Mute Restriction

**Files:**
- Create: `src/main/java/com/lumu99/forum/forum/controller/ForumCommentController.java`
- Create: `src/main/java/com/lumu99/forum/forum/service/ForumCommentService.java`
- Test: `src/test/java/com/lumu99/forum/forum/ForumCommentMuteTest.java`

**Step 1: Write the failing test**

```java
@Test
void mutedUserCannotComment() throws Exception {
    mvc.perform(post("/forum/posts/{id}/comments", approvedPostId)
       .header("Authorization", mutedUserToken)
       .contentType(APPLICATION_JSON)
       .content("{\"content\":\"hello\"}"))
       .andExpect(status().isForbidden())
       .andExpect(jsonPath("$.code").value("FORUM_403_MUTED"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ForumCommentMuteTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Implement comment list/create.
- Enforce mute only on forum posting/commenting.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ForumCommentMuteTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/forum/controller/ForumCommentController.java src/main/java/com/lumu99/forum/forum/service/ForumCommentService.java src/test/java/com/lumu99/forum/forum/ForumCommentMuteTest.java
git commit -m "feat: add forum comments with mute restriction enforcement"
```

### Task 12: Unified Vote Service (Like/Dislike Mutual Exclusion + Cancel)

**Files:**
- Create: `src/main/java/com/lumu99/forum/interaction/controller/VoteController.java`
- Create: `src/main/java/com/lumu99/forum/interaction/service/VoteService.java`
- Test: `src/test/java/com/lumu99/forum/interaction/VoteToggleTest.java`

**Step 1: Write the failing test**

```java
@Test
void likeAndDislikeMustBeMutuallyExclusive() throws Exception {
    // like
    mvc.perform(post("/forum/posts/{id}/vote", postId)
       .header("Authorization", userToken)
       .contentType(APPLICATION_JSON)
       .content("{\"voteType\":\"LIKE\"}"))
       .andExpect(status().isOk());

    // dislike should switch
    mvc.perform(post("/forum/posts/{id}/vote", postId)
       .header("Authorization", userToken)
       .contentType(APPLICATION_JSON)
       .content("{\"voteType\":\"DISLIKE\"}"))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.data.currentVote").value("DISLIKE"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=VoteToggleTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Implement generic vote command for:
  - forum post
  - photo
  - video
  - world
- Same vote again means cancel.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=VoteToggleTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/interaction src/test/java/com/lumu99/forum/interaction/VoteToggleTest.java
git commit -m "feat: implement mutual exclusive cancellable vote service"
```

### Task 13: Content Modules CRUD + Guest Visibility Controls

**Files:**
- Create: `src/main/java/com/lumu99/forum/content/controller/*`
- Create: `src/main/java/com/lumu99/forum/content/service/*`
- Test: `src/test/java/com/lumu99/forum/content/ContentVisibilityTest.java`

**Step 1: Write the failing test**

```java
@Test
void guestCannotReadWorldWhenDisabledByAdminSetting() throws Exception {
    // set world_guest_visible = false
    mvc.perform(get("/worlds"))
       .andExpect(status().isForbidden());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ContentVisibilityTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Implement CRUD for 6 modules (admin write only).
- Implement guest visibility behavior:
  - always visible modules open
  - world/events depend on admin settings.
- Add pin/unpin as admin capability.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ContentVisibilityTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/content src/test/java/com/lumu99/forum/content/ContentVisibilityTest.java
git commit -m "feat: add content module crud and guest visibility controls"
```

### Task 14: Messaging Module + User-to-User Switch

**Files:**
- Create: `src/main/java/com/lumu99/forum/message/controller/MessageController.java`
- Create: `src/main/java/com/lumu99/forum/message/service/MessageService.java`
- Test: `src/test/java/com/lumu99/forum/message/MessagePermissionTest.java`

**Step 1: Write the failing test**

```java
@Test
void userToUserMessageBlockedWhenSwitchOff() throws Exception {
    // user_dm_enabled = false
    mvc.perform(post("/messages/to/{uuid}", otherUserUuid)
      .header("Authorization", userToken)
      .contentType(APPLICATION_JSON)
      .content("{\"content\":\"hi\"}"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.code").value("MSG_403_USER_DM_DISABLED"));
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=MessagePermissionTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Threads list/read/send APIs.
- Enforce rules:
  - user-admin always allowed
  - user-user depends on setting.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=MessagePermissionTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/message src/test/java/com/lumu99/forum/message/MessagePermissionTest.java
git commit -m "feat: add messaging module with user dm switch"
```

### Task 15: Admin User Governance APIs + Audit Logging

**Files:**
- Create: `src/main/java/com/lumu99/forum/admin/controller/AdminUserGovernanceController.java`
- Create: `src/main/java/com/lumu99/forum/audit/service/AuditLogService.java`
- Create: `src/main/java/com/lumu99/forum/audit/aspect/AuditAspect.java`
- Test: `src/test/java/com/lumu99/forum/audit/AuditMaskingTest.java`

**Step 1: Write the failing test**

```java
@Test
void passwordMustBeMaskedInAuditPayload() {
    String payload = auditService.serializeWithMask(Map.of("newPassword", "Secret123"));
    assertThat(payload).doesNotContain("Secret123");
    assertThat(payload).contains("******");
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=AuditMaskingTest test`
Expected: FAIL.

**Step 3: Write minimal implementation**

- Implement admin APIs:
  - ban/unban
  - mute/unmute
  - force reset username
  - force reset password
- Implement audit logging for:
  - all admin operations
  - user-sensitive operations (username/password change, deactivation)
- Mask password fields in audit payload and runtime logs.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=AuditMaskingTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/admin/controller/AdminUserGovernanceController.java src/main/java/com/lumu99/forum/audit src/test/java/com/lumu99/forum/audit/AuditMaskingTest.java
git commit -m "feat: add admin user governance and password-masked audit logging"
```

### Task 16: OpenAPI and Business Documentation Delivery

**Files:**
- Modify: `src/main/java/com/lumu99/forum/config/OpenApiConfig.java`
- Create: `docs/api/permission-matrix.md`
- Create: `docs/api/state-transitions.md`
- Create: `docs/api/error-codes.md`
- Create: `docs/api/examples.md`
- Test: `src/test/java/com/lumu99/forum/docs/OpenApiSmokeTest.java`

**Step 1: Write the failing test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiSmokeTest {
    @Autowired TestRestTemplate rest;

    @Test
    void openApiJsonShouldBeAvailable() {
        ResponseEntity<String> res = rest.getForEntity("/v3/api-docs", String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(res.getBody()).contains("/auth/login");
    }
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -Dtest=OpenApiSmokeTest test`
Expected: FAIL before OpenAPI config/docs done.

**Step 3: Write minimal implementation**

- Configure grouped OpenAPI by module (`auth`, `admin`, `forum`, `review`, `content`, `message`).
- Add endpoint-level summaries and examples.
- Write business docs:
  - permission matrix
  - state transitions
  - error codes
  - request/response examples.

**Step 4: Run test to verify it passes**

Run: `mvn -Dtest=OpenApiSmokeTest test`
Expected: PASS.

**Step 5: Commit**

```bash
git add src/main/java/com/lumu99/forum/config/OpenApiConfig.java docs/api src/test/java/com/lumu99/forum/docs/OpenApiSmokeTest.java
git commit -m "docs: deliver openapi and complete business api documentation"
```

### Task 17: Full Verification Before Completion

**Files:**
- Modify (if needed): `README.md`

**Step 1: Run full test suite**

Run: `mvn clean test`
Expected: PASS all tests.

**Step 2: Run package verification**

Run: `mvn verify`
Expected: PASS build and checks.

**Step 3: Manual API smoke checks**

Run:
- `mvn spring-boot:run`
- open `/swagger-ui/index.html`
- validate key endpoints with sample payloads.
Expected: key workflows function end-to-end.

**Step 4: Update README runbook**

Include:
- local env requirements
- DB setup
- run/test/doc URLs
- migration instructions

**Step 5: Commit**

```bash
git add README.md
git commit -m "docs: finalize runbook after full verification"
```

---

## Suggested Commit Sequence
1. chore: bootstrap forum backend project structure
2. feat: add unified error response and request id filter
3. feat: add initial schema migration for forum backend
4. feat: add jwt security foundation and admin route guards
5. feat: implement auth and user self lifecycle APIs
6. feat: add quiz-based registration workflow
7. feat: add invite-code registration and admin invite management
8. feat: add admin governance for settings words tags and quiz
9. feat: implement forum post flow with review triggers and pin
10. feat: add admin review center approve reject workflow
11. feat: add forum comments with mute restriction enforcement
12. feat: implement mutual exclusive cancellable vote service
13. feat: add content module crud and guest visibility controls
14. feat: add messaging module with user dm switch
15. feat: add admin user governance and password-masked audit logging
16. docs: deliver openapi and complete business api documentation
17. docs: finalize runbook after full verification

