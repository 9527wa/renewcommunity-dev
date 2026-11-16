<template>
  <view class="page">
    <view class="hero">
      <view class="community" @click="chooseCommunity">
        <text class="community-icon">🏢</text>
        <text class="community-name">{{ communityName }}</text>
        <text class="community-arrow">›</text>
      </view>
      <view class="search-box">
        <text class="search-icon">⌕</text>
        <input
          v-model="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索公告标题或关键词"
          @confirm="handleSearch"
        />
        <text class="search-btn" @click="handleSearch">搜索</text>
      </view>
    </view>

    <view class="list">
      <view
        v-for="(item, index) in list"
        :key="item.id"
        class="card"
        :class="{ pinned: index === 0 && !keyword }"
        @click="openDetail(item.id)"
      >
        <image class="thumb" :src="thumbImage(index)" mode="aspectFill" />

        <view class="main">
          <view class="meta-row">
            <text v-if="index === 0 && !keyword" class="pin-tag">置顶</text>
            <text class="type">{{ item.type }}</text>
            <text class="date">{{ formatDate(item.publishedAt) }}</text>
          </view>
          <text class="title">{{ item.title }}</text>
          <text class="summary">{{ item.summary }}</text>
        </view>

        <text class="arrow">›</text>
      </view>

      <view v-if="!list.length && !loading" class="empty">暂无公告</view>
      <view v-if="hasMore && list.length" class="load-more" @click="loadMore">加载更多</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { bindCommunity, fetchAnnouncementList, fetchCommunityList, fetchProfile } from '@/api/app';
import type { AppAnnouncement, AppCommunity, AppProfile } from '@/api/app';

const thumbs = [
  '/static/home/news-road.png',
  '/static/home/news-pipe.png',
  '/static/home/news-notice.png',
  '/static/home/project-street.png'
];

const list = ref<AppAnnouncement[]>([]);
const profile = ref<AppProfile | null>(null);
const communities = ref<AppCommunity[]>([]);
const keyword = ref('');
const pageNum = ref(1);
const pageSize = 10;
const loading = ref(false);
const hasMore = ref(false);

const communityName = computed(() => profile.value?.communityName || '选择小区');

function thumbImage(index: number) {
  return thumbs[index % thumbs.length];
}

function formatDate(value?: string | null) {
  return value ? value.slice(0, 10) : '';
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

async function load(reset = false) {
  if (loading.value) {
    return;
  }

  loading.value = true;

  try {
    const page = await fetchAnnouncementList({
      keyword: keyword.value.trim() || undefined,
      pageNum: pageNum.value,
      pageSize
    });
    list.value = reset ? page.list : [...list.value, ...page.list];
    hasMore.value = list.value.length < page.total;
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
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

function openDetail(id: number) {
  uni.navigateTo({ url: `/pages/announcement/detail?id=${id}` });
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
      } catch {
        // 错误提示由 request 统一处理
      }
    }
  });
}

onLoad(async () => {
  await loadBaseData();
  await load(true);
});

onPullDownRefresh(async () => {
  pageNum.value = 1;
  await loadBaseData();
  await load(true);
  uni.stopPullDownRefresh();
});

onReachBottom(() => {
  if (hasMore.value) {
    loadMore();
  }
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.hero {
  padding: calc(20rpx + var(--status-bar-height)) 24rpx 28rpx;
  background: linear-gradient(180deg, #3d92ef 0%, #5aa6f3 55%, #86c0f9 100%);
}

.community {
  display: flex;
  align-items: center;
  align-self: flex-start;
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

.search-box {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
  height: 76rpx;
  padding: 0 24rpx;
  border-radius: 38rpx;
  background-color: #ffffff;
}

.search-icon {
  margin-right: 10rpx;
  font-size: 32rpx;
  color: #9aa4b2;
}

.search-input {
  flex: 1;
  font-size: 25rpx;
}

.search-btn {
  margin-left: 10rpx;
  padding-left: 20rpx;
  border-left: 1rpx solid #eef2f7;
  font-size: 24rpx;
  color: #1677ff;
}

.list {
  padding: 24rpx 24rpx 60rpx;
}

.card {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 20rpx;
  border-radius: 18rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.04);
}

.card.pinned {
  background: linear-gradient(135deg, #eef7ff 0%, #e3f2ff 100%);
}

.thumb {
  flex-shrink: 0;
  width: 150rpx;
  height: 110rpx;
  border-radius: 12rpx;
}

.main {
  flex: 1;
  min-width: 0;
  margin-left: 16rpx;
}

.meta-row {
  display: flex;
  align-items: center;
}

.pin-tag {
  margin-right: 10rpx;
  padding: 2rpx 10rpx;
  border-radius: 8rpx;
  background-color: #e9f8ef;
  font-size: 19rpx;
  color: #16a34a;
}

.type {
  margin-right: 12rpx;
  font-size: 20rpx;
  color: #1677ff;
}

.date {
  font-size: 20rpx;
  color: #9aa4b2;
}

.title {
  display: block;
  margin-top: 8rpx;
  font-size: 27rpx;
  font-weight: 600;
  line-height: 1.4;
  color: #1f2d3d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #8b95a4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow {
  flex-shrink: 0;
  margin-left: 10rpx;
  font-size: 30rpx;
  color: #c3cad6;
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
