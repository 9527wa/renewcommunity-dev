<template>
  <view class="page">
    <view class="list">
      <view v-for="item in list" :key="item.id" class="card" @click="openDetail(item.id)">
        <view class="row">
          <text class="type">{{ item.type }}</text>
          <text class="date">{{ formatDate(item.publishedAt) }}</text>
        </view>
        <text class="title">{{ item.title }}</text>
        <text class="summary">{{ item.summary }}</text>
      </view>

      <view v-if="!list.length && !loading" class="empty">暂无公告</view>
      <view v-if="hasMore && list.length" class="load-more" @click="loadMore">加载更多</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchAnnouncementList } from '@/api/app';
import type { AppAnnouncement } from '@/api/app';

const list = ref<AppAnnouncement[]>([]);
const pageNum = ref(1);
const pageSize = 10;
const total = ref(0);
const loading = ref(false);
const hasMore = ref(false);

function formatDate(value?: string | null) {
  return value ? value.slice(0, 10) : '';
}

async function load(reset = false) {
  if (loading.value) {
    return;
  }

  loading.value = true;

  try {
    const page = await fetchAnnouncementList({ pageNum: pageNum.value, pageSize });
    list.value = reset ? page.list : [...list.value, ...page.list];
    total.value = page.total;
    hasMore.value = list.value.length < total.value;
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    loading.value = false;
  }
}

function loadMore() {
  pageNum.value += 1;
  load();
}

function openDetail(id: number) {
  uni.navigateTo({ url: `/pages/announcement/detail?id=${id}` });
}

onLoad(() => load(true));
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.list {
  padding: 24rpx;
}

.card {
  margin-bottom: 24rpx;
  padding: 28rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.type {
  padding: 4rpx 14rpx;
  border-radius: 16rpx;
  background-color: #eaf3ff;
  font-size: 22rpx;
  color: #1677ff;
}

.date {
  font-size: 22rpx;
  color: #9aa4b2;
}

.title {
  display: block;
  margin-top: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  line-height: 1.45;
  color: #1f2d3d;
}

.summary {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #8b95a4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.empty {
  padding: 120rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #9aa4b2;
}

.load-more {
  padding: 20rpx 0 40rpx;
  text-align: center;
  font-size: 24rpx;
  color: #1677ff;
}
</style>
