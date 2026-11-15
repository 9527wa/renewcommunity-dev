<template>
  <view class="page">
    <view v-if="detail" class="card">
      <text class="title">{{ detail.title }}</text>
      <view class="meta">
        <text class="type">{{ detail.type }}</text>
        <text class="date">{{ formatDate(detail.publishedAt) }}</text>
      </view>
      <text class="content">{{ detail.content }}</text>
    </view>

    <view v-else-if="!loading" class="empty">公告不存在或已下架</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchAnnouncementDetail } from '@/api/app';
import type { AppAnnouncementDetail } from '@/api/app';

const detail = ref<AppAnnouncementDetail | null>(null);
const loading = ref(false);

function formatDate(value?: string | null) {
  return value ? value.slice(0, 10) : '';
}

onLoad(async options => {
  const id = Number(options?.id);

  if (!id) {
    return;
  }

  loading.value = true;

  try {
    detail.value = await fetchAnnouncementDetail(id);
  } catch {
    detail.value = null;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.card {
  padding: 32rpx 28rpx 40rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  line-height: 1.45;
  color: #1f2d3d;
}

.meta {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}

.type {
  padding: 4rpx 14rpx;
  border-radius: 16rpx;
  background-color: #eaf3ff;
  font-size: 22rpx;
  color: #1677ff;
}

.date {
  margin-left: 16rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}

.content {
  display: block;
  margin-top: 30rpx;
  font-size: 28rpx;
  line-height: 1.8;
  color: #4b5563;
  white-space: pre-wrap;
}

.empty {
  padding: 160rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #9aa4b2;
}
</style>
