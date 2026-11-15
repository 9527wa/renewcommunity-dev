<template>
  <view class="page">
    <view class="filters">
      <view
        v-for="filter in filters"
        :key="filter.label"
        class="filter"
        :class="{ active: statusFilter === filter.value }"
        @click="changeStatus(filter.value)"
      >
        {{ filter.label }}
      </view>
    </view>

    <view class="list">
      <view v-for="item in list" :key="item.id" class="card" @click="openDetail(item.id)">
        <view class="row">
          <text class="name">{{ item.name }}</text>
          <text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
        </view>
        <text class="community">{{ item.community || '未知小区' }} · {{ item.category || '改造项目' }}</text>
        <view class="progress-row">
          <view class="track">
            <view class="fill" :style="{ width: item.progress + '%' }" />
          </view>
          <text class="percent">{{ item.progress }}%</text>
        </view>
        <text class="plan">计划时间：{{ formatPlan(item) }}</text>
      </view>

      <view v-if="!list.length && !loading" class="empty">暂无项目</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchProjectList } from '@/api/app';
import type { AppProject } from '@/api/app';

const filters = [
  { label: '全部', value: null },
  { label: '规划中', value: 0 },
  { label: '施工中', value: 1 },
  { label: '已完成', value: 2 }
];

const list = ref<AppProject[]>([]);
const statusFilter = ref<number | null>(null);
const loading = ref(false);

function statusText(status: number) {
  if (status === 0) {
    return '规划中';
  }

  if (status === 2) {
    return '已完成';
  }

  return '施工中';
}

function statusClass(status: number) {
  if (status === 0) {
    return 'status-planning';
  }

  if (status === 2) {
    return 'status-done';
  }

  return 'status-ongoing';
}

function formatPlan(item: AppProject) {
  if (!item.startDate || !item.endDate) {
    return '待定';
  }

  return `${item.startDate.slice(0, 10)} ~ ${item.endDate.slice(0, 10)}`;
}

async function load() {
  if (loading.value) {
    return;
  }

  loading.value = true;

  try {
    const page = await fetchProjectList({
      status: statusFilter.value ?? undefined,
      pageNum: 1,
      pageSize: 50
    });
    list.value = page.list;
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    loading.value = false;
  }
}

function changeStatus(value: number | null) {
  statusFilter.value = value;
  load();
}

function openDetail(id: number) {
  uni.navigateTo({ url: `/pages/project/detail?id=${id}` });
}

onLoad(load);
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.filters {
  display: flex;
  padding: 24rpx 24rpx 0;
}

.filter {
  margin-right: 16rpx;
  padding: 10rpx 26rpx;
  border-radius: 28rpx;
  background-color: #ffffff;
  font-size: 24rpx;
  color: #5b6472;
}

.filter.active {
  background: linear-gradient(135deg, #3b9cf7 0%, #1677ff 100%);
  color: #ffffff;
  box-shadow: 0 6rpx 14rpx rgba(22, 119, 255, 0.24);
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

.name {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.status {
  flex-shrink: 0;
  margin-left: 16rpx;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
}

.status-planning {
  background-color: #f1f4f8;
  color: #6b7280;
}

.status-ongoing {
  background-color: #eaf3ff;
  color: #1677ff;
}

.status-done {
  background-color: #e9f8ef;
  color: #16a34a;
}

.community {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #8b95a4;
}

.progress-row {
  display: flex;
  align-items: center;
  margin-top: 18rpx;
}

.track {
  flex: 1;
  height: 12rpx;
  border-radius: 6rpx;
  background-color: #e8edf4;
  overflow: hidden;
}

.fill {
  height: 100%;
  border-radius: 6rpx;
  background: linear-gradient(90deg, #39c26b 0%, #22a06b 100%);
}

.percent {
  margin-left: 14rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #16a34a;
}

.plan {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}

.empty {
  padding: 120rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #9aa4b2;
}
</style>
