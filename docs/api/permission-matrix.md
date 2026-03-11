# Permission Matrix

## Roles
- `GUEST`: 未登录访客
- `USER`: 普通登录用户
- `ADMIN`: 管理员

## Access Rules

| API Area | Guest | User | Admin |
| --- | --- | --- | --- |
| `/auth/**` | Allow | Allow | Allow |
| `/users/me/**` | Deny | Allow(Self) | Allow(Self) |
| `/forum/**` | Deny | Allow | Allow |
| `/admin/**` | Deny | Deny | Allow |
| `/stories` `/timelines` `/photos` `/videos` GET | Allow | Allow | Allow |
| `/worlds` `/events` GET | 受 `admin_settings` 控制 | Allow | Allow |
| `/stories` `/timelines` `/photos` `/videos` `/worlds` `/events` 写操作 | Deny | Deny | Allow |
| `/messages/**` | Deny | Allow | Allow |

## Special Constraints
- `BANNED` 用户无法登录。
- `MUTED` 用户不能发帖/评论，但可投票、可发私信。
- 普通用户不能使用 `admin_only` 标签。
- 用户间私信受 `admin_settings.user_dm_enabled` 控制，用户-管理员私信始终允许。
