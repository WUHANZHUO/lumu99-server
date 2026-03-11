# API Examples

## 1. Login

Request:

```http
POST /auth/login
Content-Type: application/json

{
  "username": "demo_user",
  "password": "Secret123"
}
```

Response:

```json
{
  "data": {
    "token": "<jwt>",
    "userUuid": "00000000-0000-0000-0000-000000000001",
    "role": "USER"
  }
}
```

## 2. Create Forum Post (Need Review)

Request:

```http
POST /forum/posts
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "title": "Hello",
  "content": "contains_forbidden_word",
  "tagIds": []
}
```

Response:

```json
{
  "data": {
    "id": 1001,
    "reviewStatus": "PENDING"
  }
}
```

## 3. Approve Post

Request:

```http
POST /admin/reviews/posts/1001/approve
Authorization: Bearer <admin-jwt>
```

Response:

```json
{
  "data": {
    "id": 1001,
    "reviewStatus": "APPROVED"
  }
}
```

## 4. User-to-User DM Blocked

Request:

```http
POST /messages/to/00000000-0000-0000-0000-000000000002
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "content": "hi"
}
```

Response:

```json
{
  "code": "MSG_403_USER_DM_DISABLED",
  "message": "User DM is disabled",
  "requestId": "..."
}
```
