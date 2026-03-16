/**
 * 站内信 API
 */
import request from '../request'
import type { PageResult, SystemMessageVO } from '@/types/content'

const BASE = '/messages'

/** 分页查询消息 */
export function listMessages(pageNo = 1, pageSize = 20) {
  return request.get<PageResult<SystemMessageVO>>(BASE, {
    params: { pageNo, pageSize },
  })
}

/** 未读数量 */
export function getUnreadCount() {
  return request.get<number>(BASE + '/unread-count')
}

/** 标记已读 */
export function markAsRead(id: number) {
  return request.put(`${BASE}/${id}/read`)
}
