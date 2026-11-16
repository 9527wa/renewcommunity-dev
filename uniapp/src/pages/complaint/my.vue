<template>
  <view class="page">
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.label"
        class="tab"
        :class="{ active: statusFilter === tab.value }"
        @click="changeStatus(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <view class="list">
      <view v-for="item in list" :key="item.id" class="card" @click="openDetail(item)">
        <view class="card-head">
          <text class="order-no">工单编号：{{ item.orderNo || '-' }}</text>
          <text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
        </view>

        <view class="card-body">
          <image class="thumb" :src="item.photo || fallbackImage(item.id)" mode="aspectFill" />
          <view class="body-main">
            <text class="title">{{ item.title }}</text>
            <view class="meta-row">
              <text class="meta-icon">⌖</text>
              <text class="meta-text">{{ item.location || item.projectName || '未填写位置' }}</text>
            </view>
            <view class="meta-row">
              <text class="meta-icon">◷</text>
              <text class="meta-text">提交时间：{{ formatDate(item.createdAt) }}</text>
            </view>
          </view>
        </view>

        <view class="progress-box" :class="statusClass(item.status)">
          <text class="progress-label">办理进展：</text>
          <text class="progress-text">{{ progressText(item) }}</text>
          <text class="detail-link">查看详情 ›</text>
        </view>
      </view>

      <view v-if="!list.length && !loading" class="empty">暂无投诉记录</view>
      <view v-if="hasMore" class="load-more" @click="loadMore">加载更多</view>
    </view>

    <tab-bar active="complaint" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';
import { fetchMyComplaints } from '@/api/app';
import type { AppComplaint } from '@/api/app';
import TabBar from '@/components/tab-bar/tab-bar.vue';
import { clearToken, isLoggedIn } from '@/utils/auth';
import { downloadAuthImage } from '@/utils/file';

interface ComplaintWithPhoto extends AppComplaint {
  photo?: string;
}

const tabs = [
  { label: '全部', value: null },
  { label: '待受理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已处理', value: 2 }
];

const list = ref<ComplaintWithPhoto[]>([]);
const statusFilter = ref<number | null>(null);
const pageNum = ref(1);
const pageSize = 10;
const loading = ref(false);
const hasMore = ref(false);

const fallbackImages = [
  '/static/home/project-street.png',
  '/static/home/news-road.png',
  '/static/home/news-notice.png'
];

function fallbackImage(id: number) {
  return fallbackImages[id % fallbackImages.length];
}

function statusText(status: number) {
  if (status === 0) {
    return '待受理';
  }

  if (status === 1) {
    return '处理中';
  }

  return '已处理';
}

function statusClass(status: number) {
  if (status === 0) {
    return 'status-pending';
  }

  if (status === 1) {
    return 'status-processing';
  }

  return 'status-done';
}

function progressText(item: AppComplaint) {
  if (item.reply) {
    return item.reply;
  }

  if (item.status === 0) {
    return '您的诉求已提交，系统正在分派至相关部门，请耐心等待受理。';
  }

  if (item.status === 1) {
    return '工作人员正在处理您的诉求，请耐心等待处理结果。';
  }

  return '您的诉求已办结，感谢您的反馈。';
}

function formatDate(value?: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

function redirectToLogin() {
  clearToken();
  uni.reLaunch({ url: '/pages/login/login' });
}

async function attachPhotos(items: AppComplaint[]) {
  return Promise.all(
    items.map(async item => {
      let photo = '';

      if (item.images?.length) {
        try {
          photo = await downloadAuthImage(item.images[0].fileId);
        } catch {
          photo = '';
        }
      }

      return { ...item, photo };
    })
  );
}

async function load(reset = false) {
  if (loading.value) {
    return;
  }

  loading.value = true;

  try {
    const page = await fetchMyComplaints({
      status: statusFilter.value ?? undefined,
      pageNum: pageNum.value,
      pageSize
    });
    const items = await attachPhotos(page.list);
    list.value = reset ? items : [...list.value, ...items];
    hasMore.value = list.value.length < page.total;
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    loading.value = false;
  }
}

function changeStatus(value: number | null) {
  statusFilter.value = value;
  pageNum.value = 1;
  load(true);
}

function loadMore() {
  if (!hasMore.value) {
    return;
  }

  pageNum.value += 1;
  load();
}

function openDetail(item: AppComplaint) {
  uni.navigateTo({ url: `/pages/complaint/detail?id=${item.id}` });
}

onLoad(options => {
  if (!isLoggedIn()) {
    redirectToLogin();
    return;
  }

  if (options?.status !== undefined && options?.status !== '') {
    const status = Number(options.status);
    statusFilter.value = Number.isNaN(status) ? null : status;
  }

  load(true);
});

onShow(() => {
  if (isLoggedIn()) {
    pageNum.value = 1;
    load(true);
  }
});

onPullDownRefresh(async () => {
  pageNum.value = 1;
  await load(true);
  uni.stopPullDownRefresh();
});

onReachBottom(loadMore);
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.tabs {
  display: flex;
  padding: 20rpx 16rpx;
  background-color: #ffffff;
}

.tab {
  flex: 1;
  height: 68rpx;
  line-height: 68rpx;
  margin: 0 8rpx;
  border-radius: 34rpx;
  text-align: center;
  font-size: 25rpx;
  color: #5b6472;
}

.tab.active {
  background: linear-gradient(135deg, #2fb47a 0%, #22a06b 100%);
  color: #ffffff;
  font-weight: 600;
}

.list {
  padding: 24rpx 24rpx 200rpx;
}

.card {
  margin-bottom: 24rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.order-no {
  font-size: 22rpx;
  color: #9aa4b2;
}

.status {
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
}

.status-pending {
  background-color: #fff7e6;
  color: #d97706;
}

.status-processing {
  background-color: #e9f8ef;
  color: #16a34a;
}

.status-done {
  background-color: #e9f8ef;
  color: #15803d;
}

.card-body {
  display: flex;
  margin-top: 18rpx;
}

.thumb {
  flex-shrink: 0;
  width: 180rpx;
  height: 130rpx;
  border-radius: 12rpx;
}

.body-main {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
}

.title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1.4;
  color: #1f2d3d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-row {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
}

.meta-icon {
  margin-right: 6rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}

.meta-text {
  flex: 1;
  font-size: 22rpx;
  color: #8b95a4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-box {
  margin-top: 18rpx;
  padding: 16rpx 18rpx;
  border-radius: 12rpx;
  background-color: #f5f9ff;
}

.progress-label {
  font-size: 23rpx;
  font-weight: 600;
  color: #1677ff;
}

.progress-text {
  font-size: 23rpx;
  line-height: 1.6;
  color: #5b6472;
}

.detail-link {
  display: block;
  margin-top: 8rpx;
  text-align: right;
  font-size: 23rpx;
  color: #1677ff;
}

.empty {
  padding: 140rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #9aa4b2;
}

.load-more {
  padding: 20rpx 0;
  text-align: center;
  font-size: 24rpx;
  color: #1677ff;
}
</style>
