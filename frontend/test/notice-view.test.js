import test from 'node:test'
import assert from 'node:assert/strict'
import {
  buildNoticeFeed,
  getNoticeStatusMeta,
  getNoticeTypeMeta,
  summarizeNoticeFeed
} from '../src/utils/notice-view.js'

test('builds a feed with pinned-first and newest-first ordering', () => {
  const feed = buildNoticeFeed([
    { id: 1, title: '普通公告', content: '普通内容', noticeType: 1, publishStatus: 1, topFlag: 0, publishTime: '2026-09-16T09:00:00' },
    { id: 2, title: '置顶公告', content: '置顶内容', noticeType: 2, publishStatus: 1, topFlag: 1, publishTime: '2026-09-15T09:00:00' },
    { id: 3, title: '最新公告', content: '最新内容', noticeType: 3, publishStatus: 1, topFlag: 0, publishTime: '2026-09-17T09:00:00' }
  ], [3])

  assert.deepEqual(feed.map((item) => item.id), [2, 3, 1])
  assert.equal(feed[1].isRead, true)
  assert.equal(feed[0].typeLabel, '制度')
  assert.equal(feed[0].statusLabel, '已发布')
})

test('normalizes content into a bounded summary', () => {
  const [item] = buildNoticeFeed([
    { id: 1, title: '公告', content: '  第一行\n\n第二行   内容，这是一段较长的公告正文。  ', noticeType: 4, publishStatus: 0 }
  ], [], { summaryLength: 17 })

  assert.equal(item.summary, '第一行 第二行 内容，这是一段较长…')
})

test('returns stable type and status metadata with fallbacks', () => {
  assert.deepEqual(getNoticeTypeMeta(3), { label: '活动', tag: 'success' })
  assert.deepEqual(getNoticeTypeMeta(99), { label: '其他', tag: 'info' })
  assert.deepEqual(getNoticeStatusMeta(2), { label: '已下线', tag: 'warning' })
  assert.deepEqual(getNoticeStatusMeta(99), { label: '未知状态', tag: 'info' })
})

test('summarizes total, unread and pinned notices', () => {
  const summary = summarizeNoticeFeed([
    { id: 1, topFlag: 1, isRead: false },
    { id: 2, topFlag: 0, isRead: true },
    { id: 3, topFlag: 0, isRead: false }
  ])

  assert.deepEqual(summary, { total: 3, unread: 2, pinned: 1 })
})

test('uses the server read flag while keeping local read state as an immediate fallback', () => {
  const feed = buildNoticeFeed([
    { id: 1, title: '服务端已读', readFlag: true },
    { id: 2, title: '本地刚读', readFlag: false }
  ], [2])

  assert.equal(feed.find((item) => item.id === 1).isRead, true)
  assert.equal(feed.find((item) => item.id === 2).isRead, true)
})
