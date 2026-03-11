# Error Codes

统一结构：

```json
{
  "code": "AUTH_401_UNAUTHORIZED",
  "message": "Unauthorized",
  "requestId": "..."
}
```

## Auth
- `AUTH_401_UNAUTHORIZED`: 未认证或 Token 无效
- `AUTH_403_FORBIDDEN`: 已认证但无资源访问权限
- `ADMIN_403_ONLY_ADMIN`: 非管理员访问管理员接口
- `LOGIN_401_INVALID_CREDENTIALS`: 登录用户名或密码错误
- `LOGIN_403_ACCOUNT_BANNED`: 账号被封禁
- `LOGIN_403_ACCOUNT_DEACTIVATED`: 账号已注销

## Registration
- `REG_409_USERNAME_EXISTS`: 用户名已存在
- `REG_409_WEIBO_EXISTS`: 微博名已存在
- `REG_422_QUIZ_ANSWER_WRONG`: 题目答案错误
- `REG_422_INVITE_CODE_INVALID`: 邀请码不可用（已用/过期/禁用）

## Forum and Review
- `FORUM_403_MUTED`: 禁言用户不能发帖/评论
- `FORUM_422_TAG_NOT_ALLOWED`: 普通用户使用了管理员标签
- `REVIEW_404_POST_NOT_PENDING`: 审核目标不在待审核状态

## Message
- `MSG_403_USER_DM_DISABLED`: 用户间私信开关关闭

## Common
- `REQ_400_BAD_REQUEST`: 请求参数不合法
- `REQ_404_NOT_FOUND`: 资源不存在
- `SYS_500_INTERNAL_ERROR`: 未预期内部错误
