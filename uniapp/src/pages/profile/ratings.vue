<template>
  <view class="page">
    <view class="list">
      <view v-for="item in list" :key="item.id" class="card">
        <view class="head">
          <text class="title">{{ item.complaintTitle || '投诉评价' }}</text>
          <text class="date">{{ formatDate(item.createdAt) }}</text>
        </view>
        <view class="stars">
          <text v-for="n in 5" :key="n" class="star" :class="{ active: n <= item.score }">★</text>
        </view>
        <text class="comment">{{ item.comment || '未填写评价内容' }}</text>
      </view>

      <view v-if="!list.length && !loading" class="empty">暂无评价</view>
      <view v-if="hasMore" class="load-more" @click="loadMore">加载更多</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onReachBottom } from '@dcloudio/uni-app';
import { fetchMyRatings } from '@/api/app';
import type { MySatisfaction } from '@/api/app';

const list = ref<MySatisfaction[]>([]);
const pageNum = ref(1);
const pageSize = 10;
const loading = ref(false);
const hasMore = ref(false);

function formatDate(value?: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

async function load(reset = false) {
  if (loading.value) {
    return;
  }

  loading.value = true;

  try {
    const page = await fetchMyRatings({ pageNum: pageNum.value, pageSize });
    list.value = reset ? page.list : [...list.value, ...page.list];
    hasMore.value = list.value.length < page.total;
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    loading.value = false;
  }
}

function loadMore() {
  if (!hasMore.value) {
    return;
  }

  pageNum.value += 1;
  load();
}

onLoad(() => load(true));
onReachBottom(loadMore);
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
  padding: 26rpx 24rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  flex: 1;
  font-size: 27rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.date {
  flex-shrink: 0;
  margin-left: 16rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}

.stars {
  display: flex;
  margin-top: 14rpx;
}

.star {
  margin-right: 6rpx;
  font-size: 30rpx;
  color: #d7dde7;
}

.star.active {
  color: #f59e0b;
}

.comment {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #8b95a4;
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
