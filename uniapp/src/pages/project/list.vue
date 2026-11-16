<template>
  <view class="page">
    <view class="hero">
      <view class="hero-row">
        <view class="community" @click="chooseCommunity">
          <text class="community-icon">🏢</text>
          <text class="community-name">{{ communityName }}</text>
          <text class="community-arrow">›</text>
        </view>
        <text class="search-icon" @click="searchVisible = !searchVisible">⌕</text>
      </view>
      <view v-if="searchVisible" class="search-box">
        <input
          v-model="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索项目名称 / 小区 / 类别"
          @confirm="handleSearch"
        />
        <text class="search-btn" @click="handleSearch">搜索</text>
      </view>
    </view>

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
      <view v-for="item in list" :key="item.id" class="card" @click="openDetail(item.id)">
        <image class="cover" :src="coverImage(item.id)" mode="aspectFill" />

        <view class="card-main">
          <view class="title-row">
            <text class="title">{{ item.name }}</text>
            <text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
          </view>

          <view class="info-row">
            <text class="info-icon">▤</text>
            <text class="info-text">计划时间：{{ formatPlan(item) }}</text>
          </view>
          <view class="info-row">
            <text class="info-icon">▣</text>
            <text class="info-text ellipsis">改造内容：{{ contentText(item) }}</text>
          </view>

          <view class="progress-row">
            <text class="progress-label">项目进度</text>
            <view class="track">
              <view class="fill" :style="{ width: item.progress + '%' }" />
            </view>
            <text class="percent">{{ item.progress }}%</text>
          </view>

          <view class="detail-row">
            <text class="detail-btn">查看详情</text>
          </view>
        </view>
      </view>

      <view v-if="!list.length && !loading" class="empty">暂无项目</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { bindCommunity, fetchCommunityList, fetchProfile, fetchProjectList } from '@/api/app';
import type { AppCommunity, AppProfile, AppProject } from '@/api/app';

const tabs = [
  { label: '全部', value: null },
  { label: '施工中', value: 1 },
  { label: '准备中', value: 0 },
  { label: '已完工', value: 2 }
];

const projectImages = [
  '/static/home/project-street.png',
  '/static/home/news-road.png',
  '/static/home/news-notice.png',
  '/static/home/banner-scene.png'
];

const list = ref<AppProject[]>([]);
const profile = ref<AppProfile | null>(null);
const communities = ref<AppCommunity[]>([]);
const statusFilter = ref<number | null>(null);
const keyword = ref('');
const searchVisible = ref(false);
const loading = ref(false);

const communityName = computed(() => profile.value?.communityName || '选择小区');

function coverImage(id: number) {
  return projectImages[id % projectImages.length];
}

function statusText(status: number) {
  if (status === 0) {
    return '准备中';
  }

  if (status === 2) {
    return '已完工';
  }

  return '施工中';
}

function statusClass(status: number) {
  if (status === 0) {
    return 'status-preparing';
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

  return `${item.startDate.slice(0, 7).replace('-', '.')} - ${item.endDate.slice(0, 7).replace('-', '.')}`;
}

function contentText(item: AppProject) {
  return item.description || item.category || '小区改造提升';
}

async function loadBaseData() {
  try {
    const [profileData, communityList] = await Promise.all([fetchProfile(), fetchCommunityList()]);
    profile.value = profileData;
    communities.value = communityList;
  } catch {
    // 错误提示由 request 统一处理
  }
}

async function load() {
  if (loading.value) {
    return;
  }

  loading.value = true;

  try {
    const page = await fetchProjectList({
      status: statusFilter.value ?? undefined,
      community: profile.value?.communityName || undefined,
      keyword: keyword.value.trim() || undefined,
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

function handleSearch() {
  load();
}

function openDetail(id: number) {
  uni.navigateTo({ url: `/pages/project/detail?id=${id}` });
}

function chooseCommunity() {
  if (!communities.value.length) {
    uni.showToast({ title: '暂无可选择的小区', icon: 'none' });
    return;
  }

  uni.showActionSheet({
    itemList: communities.value.map(item => item.name),
    success: async res => {
      const community = communities.value[res.tapIndex];

      if (!community) {
        return;
      }

      try {
        await bindCommunity(community.id);
        profile.value = await fetchProfile();
        uni.showToast({ title: `已切换到${community.name}`, icon: 'none' });
        await load();
      } catch {
        // 错误提示由 request 统一处理
      }
    }
  });
}

onLoad(async () => {
  await loadBaseData();
  await load();
});

onPullDownRefresh(async () => {
  await loadBaseData();
  await load();
  uni.stopPullDownRefresh();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.hero {
  padding: calc(24rpx + var(--status-bar-height)) 24rpx 28rpx;
  background: linear-gradient(180deg, #3d92ef 0%, #5aa6f3 55%, #86c0f9 100%);
}

.hero-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.community {
  display: flex;
  align-items: center;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  background-color: rgba(255, 255, 255, 0.22);
}

.community-icon {
  margin-right: 8rpx;
  font-size: 26rpx;
}

.community-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
}

.community-arrow {
  margin-left: 8rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.search-icon {
  font-size: 40rpx;
  color: #ffffff;
}

.search-box {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
}

.search-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
  font-size: 25rpx;
  background-color: #ffffff;
  border-radius: 36rpx;
}

.search-btn {
  margin-left: 14rpx;
  padding: 0 26rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 36rpx;
  background-color: rgba(255, 255, 255, 0.9);
  font-size: 25rpx;
  color: #1677ff;
}

.tabs {
  display: flex;
  padding: 20rpx 16rpx;
  background-color: #ffffff;
}

.tab {
  flex: 1;
  height: 66rpx;
  line-height: 66rpx;
  margin: 0 8rpx;
  border-radius: 33rpx;
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
  padding: 24rpx;
}

.card {
  display: flex;
  margin-bottom: 24rpx;
  padding: 20rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.cover {
  flex-shrink: 0;
  width: 200rpx;
  height: 190rpx;
  border-radius: 14rpx;
}

.card-main {
  flex: 1;
  min-width: 0;
  margin-left: 18rpx;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.title {
  flex: 1;
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1.4;
  color: #1f2d3d;
}

.status {
  flex-shrink: 0;
  margin-left: 10rpx;
  padding: 2rpx 12rpx;
  border-radius: 14rpx;
  font-size: 21rpx;
}

.status-preparing {
  background-color: #eef2f7;
  color: #5b6472;
}

.status-ongoing {
  background-color: #e9f8ef;
  color: #16a34a;
}

.status-done {
  background-color: #e9f8ef;
  color: #15803d;
}

.info-row {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
}

.info-icon {
  margin-right: 6rpx;
  font-size: 20rpx;
  color: #9aa4b2;
}

.info-text {
  flex: 1;
  font-size: 21rpx;
  color: #8b95a4;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-row {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
}

.progress-label {
  flex-shrink: 0;
  margin-right: 10rpx;
  font-size: 21rpx;
  color: #6b7280;
}

.track {
  flex: 1;
  height: 10rpx;
  border-radius: 5rpx;
  background-color: #e8edf4;
  overflow: hidden;
}

.fill {
  height: 100%;
  border-radius: 5rpx;
  background: linear-gradient(90deg, #39c26b 0%, #22a06b 100%);
}

.percent {
  margin-left: 10rpx;
  font-size: 22rpx;
  font-weight: 600;
  color: #16a34a;
}

.detail-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 10rpx;
}

.detail-btn {
  padding: 4rpx 20rpx;
  border: 2rpx solid #9fd9bd;
  border-radius: 20rpx;
  font-size: 21rpx;
  color: #22a06b;
}

.empty {
  padding: 120rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #9aa4b2;
}
</style>
