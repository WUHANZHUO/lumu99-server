# State Transitions

## Account Status
- `ACTIVE -> BANNED`
- `BANNED -> ACTIVE`
- `ACTIVE -> DEACTIVATED`
- `BANNED -> DEACTIVATED`

## Mute Status
- `NORMAL -> MUTED`
- `MUTED -> NORMAL`

## Review Status (Forum Post)
- `PENDING -> APPROVED`
- `PENDING -> REJECTED`
- `APPROVED`/`REJECTED` 不可再次进入审核流（V1）

## Invite Code Status
- `UNUSED -> USED`
- `UNUSED -> EXPIRED`
- `UNUSED -> DISABLED`

## Vote State
- `NONE -> LIKE`
- `NONE -> DISLIKE`
- `LIKE -> NONE`（重复点赞取消）
- `DISLIKE -> NONE`（重复点踩取消）
- `LIKE -> DISLIKE`
- `DISLIKE -> LIKE`
