<template>
  <view v-if="project" class="page">
    <view class="card hero-card">
      <view class="row">
        <text class="title">{{ project.name }}</text>
        <text class="status" :class="statusClass(project.status)">{{ statusText(project.status) }}</text>
      </view>
      <text class="community">{{ project.community || '未知小区' }} · {{ project.category || '改造项目' }}</text>
      <view class="progress-row">
        <view class="track">
          <view class="fill" :style="{ width: project.progress + '%' }" />
        </view>
        <text class="percent">{{ project.progress }}%</text>
      </view>
    </view>

    <view class="card">
      <text class="section-title">项目信息</text>
      <view class="info-row">
        <text class="label">所属小区</text>
        <text class="value">{{ project.community || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="label">改造类别</text>
        <text class="value">{{ project.category || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="label">计划开始</text>
        <text class="value">{{ project.startDate || '待定' }}</text>
      </view>
      <view class="info-row">
        <text class="label">计划结束</text>
        <text class="value">{{ project.endDate || '待定' }}</text>
      </view>
    </view>

    <view class="card">
      <text class="section-title">项目简介</text>
      <text class="description">{{ project.description || '暂无项目简介' }}</text>
    </view>

    <view class="actions">
      <view class="action-btn" @click="goComplaint">投诉建议</view>
      <view class="action-btn secondary" @click="goProjects">全部项目</view>
    </view>
  </view>

  <view v-else-if="!loading" class="empty-page">
    <text class="empty">项目不存在</text>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchProjectDetail } from '@/api/app';
import type { AppProject } from '@/api/app';

const project = ref<AppProject | null>(null);
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

function goComplaint() {
  uni.reLaunch({ url: `/pages/complaint/index?projectId=${project.value?.id ?? ''}` });
}

function goProjects() {
  uni.navigateTo({ url: '/pages/project/list' });
}

onLoad(async options => {
  const id = Number(options?.id);

  if (!id) {
    return;
  }

  loading.value = true;

  try {
    project.value = await fetchProjectDetail(id);
  } catch {
    project.value = null;
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

.title {
  flex: 1;
  font-size: 34rpx;
  font-weight: 700;
  line-height: 1.4;
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
  margin-top: 14rpx;
  font-size: 24rpx;
  color: #8b95a4;
}

.progress-row {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
}

.track {
  flex: 1;
  height: 14rpx;
  border-radius: 7rpx;
  background-color: #e8edf4;
  overflow: hidden;
}

.fill {
  height: 100%;
  border-radius: 7rpx;
  background: linear-gradient(90deg, #39c26b 0%, #22a06b 100%);
}

.percent {
  margin-left: 16rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #16a34a;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f1f4f8;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  font-size: 26rpx;
  color: #6b7280;
}

.value {
  font-size: 26rpx;
  color: #1f2d3d;
}

.description {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #4b5563;
}

.actions {
  display: flex;
  margin-top: 32rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  text-align: center;
  font-size: 28rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #3b9cf7 0%, #1677ff 100%);
  box-shadow: 0 8rpx 20rpx rgba(22, 119, 255, 0.24);
}

.action-btn.secondary {
  margin-left: 24rpx;
  color: #1677ff;
  background-color: #ffffff;
  border: 2rpx solid #dbeafe;
  box-shadow: none;
}

.empty-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #eef2f7;
}

.empty {
  font-size: 26rpx;
  color: #9aa4b2;
}
</style>
