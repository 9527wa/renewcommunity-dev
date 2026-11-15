<template>
  <view class="page">
    <view class="header">
      <view class="header-top">
        <view class="community" @click="handleCommunity">
          <text class="community-name">幸福家园小区</text>
          <text class="community-arrow">▾</text>
        </view>
        <text class="community-sub">老旧小区改造服务平台</text>
      </view>
      <image class="header-scene" src="/static/home/header-scene.png" mode="aspectFit" />
    </view>

    <view class="content">
      <swiper
        class="banner-swiper"
        circular
        autoplay
        :interval="4000"
        indicator-dots
        indicator-color="rgba(22, 119, 255, 0.22)"
        indicator-active-color="#1677ff"
      >
        <swiper-item v-for="(item, index) in banners" :key="index">
          <view class="banner-item">
            <view class="banner-text">
              <text class="banner-title">{{ item.title }}</text>
              <text class="banner-desc">{{ item.desc }}</text>
              <view class="banner-btn-row">
                <view class="banner-btn" @click="openAnnouncement(item.id)">
                  <text>查看详情</text>
                  <text class="banner-btn-arrow">›</text>
                </view>
              </view>
            </view>
            <image class="banner-scene" src="/static/home/banner-scene.png" mode="aspectFit" />
          </view>
        </swiper-item>
      </swiper>

      <view class="card">
        <view class="card-header">
          <view class="card-title-wrap">
            <image class="card-title-icon" src="/static/home/icon-layers.png" mode="aspectFit" />
            <text class="card-title">当前改造项目</text>
          </view>
          <view class="card-more" @click="handleProjectDetail">
            <text>项目详情</text>
            <text class="chevron">›</text>
          </view>
        </view>

        <view class="project-body">
          <image class="project-img" src="/static/home/project-street.png" mode="aspectFill" />
          <view class="project-info">
            <text class="project-title">{{ currentProject?.name || '暂无改造项目' }}</text>
            <view class="project-tag">
              <image class="project-tag-icon" src="/static/home/icon-hammer.png" mode="aspectFit" />
              <text>{{ projectStatusLabel }}</text>
            </view>
            <view class="progress-row">
              <text class="progress-label">项目进度</text>
              <view class="progress-track">
                <view class="progress-fill" :style="{ width: project.progress + '%' }" />
              </view>
              <text class="progress-value">{{ project.progress }}%</text>
            </view>
            <view class="plan-row">
              <image class="plan-icon" src="/static/home/icon-calendar.png" mode="aspectFit" />
              <text>计划时间：{{ project.plan }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="card-header">
          <view class="card-title-wrap">
            <image class="card-title-icon" src="/static/home/icon-chart.png" mode="aspectFit" />
            <text class="card-title">工程进度</text>
          </view>
        </view>

        <view class="stage-row">
          <view v-for="(stage, index) in stages" :key="stage.name" class="stage-item">
            <view class="stage" :class="{ 'stage-active': stage.active }">
              <image class="stage-icon" :src="stage.icon" mode="aspectFit" />
              <view class="stage-text">
                <text class="stage-name">{{ stage.name }}</text>
                <text class="stage-status">{{ stage.status }}</text>
              </view>
            </view>
            <text v-if="index < stages.length - 1" class="stage-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="card actions-card">
        <view v-for="action in actions" :key="action.name" class="action-item" @click="handleAction(action.name)">
          <image class="action-icon" :src="action.icon" mode="aspectFit" />
          <text class="action-label">{{ action.name }}</text>
        </view>
      </view>

      <view class="card">
        <view class="card-header">
          <view class="card-title-wrap">
            <image class="card-title-icon" src="/static/home/icon-clock.png" mode="aspectFit" />
            <text class="card-title">最新动态</text>
          </view>
          <view class="card-more" @click="goAnnouncements">
            <text>查看更多</text>
            <text class="chevron">›</text>
          </view>
        </view>

        <view class="news-list">
          <view v-for="item in news" :key="item.id" class="news-item" @click="openAnnouncement(item.id)">
            <image class="news-img" :src="item.image" mode="aspectFill" />
            <view class="news-main">
              <text class="news-title">{{ item.title }}</text>
              <text class="news-desc">{{ item.desc }}</text>
            </view>
            <text class="news-date">{{ item.date }}</text>
          </view>
        </view>
      </view>
    </view>

    <tab-bar active="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { fetchAnnouncementList, fetchProjectList } from '@/api/app';
import type { AppAnnouncement, AppProject } from '@/api/app';
import TabBar from '@/components/tab-bar/tab-bar.vue';
import { clearToken, isLoggedIn } from '@/utils/auth';

const newsImages = ['/static/home/news-pipe.png', '/static/home/news-road.png', '/static/home/news-notice.png'];

const announcements = ref<AppAnnouncement[]>([]);
const projects = ref<AppProject[]>([]);

const banners = computed(() =>
  announcements.value.length
    ? announcements.value.slice(0, 3).map(item => ({
        id: item.id,
        title: item.title,
        desc: item.summary || '点击查看公告详情'
      }))
    : [{ id: 0, title: '欢迎使用老旧小区改造服务平台', desc: '公告发布后将在这里展示，敬请关注' }]
);

const currentProject = computed<AppProject | null>(
  () => projects.value.find(item => item.status === 1) ?? projects.value[0] ?? null
);

const projectStatusLabel = computed(() => {
  const status = currentProject.value?.status;

  if (status === 0) {
    return '规划中';
  }

  if (status === 2) {
    return '已完成';
  }

  return '施工中';
});

const project = computed(() => ({
  progress: currentProject.value?.progress ?? 0,
  plan: formatPlan(currentProject.value)
}));

const stages = computed(() => deriveStages(currentProject.value?.status));

const actions = [
  { name: '投诉建议', icon: '/static/home/icon-chat.png' },
  { name: '办理进度', icon: '/static/home/icon-clipboard.png' },
  { name: '项目公示', icon: '/static/home/icon-docsearch.png' },
  { name: '公告通知', icon: '/static/home/icon-megaphone.png' }
];

const news = computed(() =>
  announcements.value.slice(0, 3).map((item, index) => ({
    id: item.id,
    title: item.title,
    desc: item.summary || '',
    date: formatMonthDay(item.publishedAt),
    image: newsImages[index % newsImages.length]
  }))
);

function deriveStages(status?: number) {
  const preparation = status === undefined ? '待启动' : status === 0 ? '进行中' : '已完成';
  const construction = status === undefined ? '待启动' : status === 0 ? '待启动' : status === 1 ? '正在进行中' : '已完成';
  const acceptance = status === 2 ? '已完成' : '待启动';

  return [
    { name: '准备阶段', status: preparation, icon: '/static/home/icon-check-gray.png', active: status === 0 },
    { name: '施工阶段', status: construction, icon: '/static/home/icon-helmet-green.png', active: status === 1 },
    { name: '验收阶段', status: acceptance, icon: '/static/home/icon-flag-gray.png', active: status === 2 }
  ];
}

function formatPlan(item?: AppProject | null) {
  if (!item?.startDate || !item?.endDate) {
    return '待定';
  }

  return `${item.startDate.slice(0, 7).replace('-', '.')} - ${item.endDate.slice(0, 7).replace('-', '.')}`;
}

function formatMonthDay(value?: string | null) {
  return value ? value.slice(5, 10) : '';
}

async function loadData() {
  try {
    const [announcementPage, projectPage] = await Promise.all([
      fetchAnnouncementList({ pageNum: 1, pageSize: 5 }),
      fetchProjectList({ pageNum: 1, pageSize: 50 })
    ]);

    announcements.value = announcementPage.list;
    projects.value = projectPage.list;
  } catch {
    // 错误提示由 request 统一处理
  }
}

function redirectToLogin() {
  clearToken();
  uni.reLaunch({ url: '/pages/login/login' });
}

onShow(() => {
  if (!isLoggedIn()) {
    redirectToLogin();
    return;
  }

  loadData();
});

function handleCommunity() {
  uni.showToast({ title: '切换小区功能建设中', icon: 'none' });
}

function openAnnouncement(id: number) {
  if (!id) {
    uni.showToast({ title: '暂无公告详情', icon: 'none' });
    return;
  }

  uni.navigateTo({ url: `/pages/announcement/detail?id=${id}` });
}

function goAnnouncements() {
  uni.navigateTo({ url: '/pages/announcement/list' });
}

function goProjects() {
  uni.navigateTo({ url: '/pages/project/list' });
}

function handleProjectDetail() {
  if (!currentProject.value) {
    uni.showToast({ title: '暂无改造项目', icon: 'none' });
    return;
  }

  uni.navigateTo({ url: `/pages/project/detail?id=${currentProject.value.id}` });
}

function handleAction(name: string) {
  if (name === '投诉建议' || name === '办理进度') {
    uni.reLaunch({ url: '/pages/complaint/index' });
    return;
  }

  if (name === '项目公示') {
    goProjects();
    return;
  }

  if (name === '公告通知') {
    goAnnouncements();
    return;
  }

  uni.showToast({ title: `${name}功能建设中`, icon: 'none' });
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.header {
  position: relative;
  height: 360rpx;
  padding: calc(28rpx + var(--status-bar-height)) 32rpx 0;
  box-sizing: border-box;
  overflow: hidden;
  background: linear-gradient(180deg, #3d92ef 0%, #5aa6f3 55%, #86c0f9 100%);
}

.header-top {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
}

.community {
  display: flex;
  align-items: center;
}

.community-name {
  font-size: 38rpx;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1rpx;
}

.community-arrow {
  margin-left: 10rpx;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

.community-sub {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.86);
}

.header-scene {
  position: absolute;
  top: 20rpx;
  right: -8rpx;
  width: 320rpx;
  height: 240rpx;
}

.content {
  position: relative;
  z-index: 3;
  margin-top: -220rpx;
  padding: 0 24rpx 200rpx;
}

.banner-swiper {
  height: 240rpx;
  border-radius: 20rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #f2f9ff 0%, #dcecff 100%);
  box-shadow: 0 10rpx 30rpx rgba(31, 84, 145, 0.10);
}

.banner-item {
  position: relative;
  display: flex;
  height: 240rpx;
  padding: 24rpx 22rpx 40rpx;
  box-sizing: border-box;
}

.banner-text {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  width: 66%;
}

.banner-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #0b4f8a;
  line-height: 1.3;
}

.banner-desc {
  margin-top: 10rpx;
  font-size: 23rpx;
  line-height: 1.55;
  color: #4a5568;
}

.banner-btn-row {
  display: flex;
  margin-top: 16rpx;
}

.banner-btn {
  display: flex;
  align-items: center;
  height: 52rpx;
  padding: 0 22rpx;
  border-radius: 26rpx;
  background: linear-gradient(135deg, #3b9cf7 0%, #1677ff 100%);
  color: #ffffff;
  font-size: 24rpx;
  box-shadow: 0 6rpx 14rpx rgba(22, 119, 255, 0.28);
}

.banner-btn-arrow {
  margin-left: 8rpx;
  font-size: 26rpx;
  line-height: 1;
}

.banner-scene {
  position: absolute;
  right: -8rpx;
  bottom: -6rpx;
  width: 286rpx;
  height: 187rpx;
}

.card {
  margin-top: 20rpx;
  padding: 20rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title-wrap {
  display: flex;
  align-items: center;
}

.card-title-icon {
  width: 38rpx;
  height: 38rpx;
  margin-right: 12rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.card-more {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #8b95a4;
}

.chevron {
  margin-left: 4rpx;
  font-size: 30rpx;
  line-height: 1;
  color: #b0b8c4;
}

.project-body {
  display: flex;
  margin-top: 20rpx;
}

.project-img {
  flex-shrink: 0;
  width: 184rpx;
  height: 134rpx;
  border-radius: 12rpx;
}

.project-info {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  margin-left: 20rpx;
}

.project-title {
  font-size: 27rpx;
  font-weight: 600;
  line-height: 1.35;
  color: #1f2d3d;
}

.project-tag {
  display: flex;
  align-self: flex-start;
  align-items: center;
  margin-top: 10rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  background-color: #e9f8ef;
  font-size: 22rpx;
  color: #16a34a;
}

.project-tag-icon {
  width: 24rpx;
  height: 24rpx;
  margin-right: 6rpx;
}

.progress-row {
  display: flex;
  align-items: center;
  margin-top: 14rpx;
}

.progress-label {
  flex-shrink: 0;
  margin-right: 10rpx;
  font-size: 22rpx;
  color: #6b7280;
}

.progress-track {
  flex: 1;
  height: 12rpx;
  border-radius: 6rpx;
  overflow: hidden;
  background-color: #e8edf4;
}

.progress-fill {
  height: 100%;
  border-radius: 6rpx;
  background: linear-gradient(90deg, #39c26b 0%, #22a06b 100%);
}

.progress-value {
  margin-left: 10rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #16a34a;
}

.plan-row {
  display: flex;
  align-items: center;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: #8b95a4;
}

.plan-icon {
  width: 24rpx;
  height: 24rpx;
  margin-right: 6rpx;
}

.stage-row {
  display: flex;
  align-items: center;
  margin-top: 18rpx;
}

.stage-item {
  display: flex;
  flex: 1;
  align-items: center;
  min-width: 0;
}

.stage {
  display: flex;
  flex: 1;
  align-items: center;
  min-width: 0;
  padding: 14rpx 10rpx;
  border-radius: 14rpx;
  background-color: #f4f6fa;
}

.stage-active {
  background: linear-gradient(135deg, #33b87c 0%, #1f9d68 100%);
  box-shadow: 0 6rpx 16rpx rgba(31, 157, 104, 0.22);
}

.stage-icon {
  flex-shrink: 0;
  width: 36rpx;
  height: 36rpx;
  margin-right: 8rpx;
}

.stage-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stage-name {
  font-size: 24rpx;
  color: #4b5563;
  white-space: nowrap;
}

.stage-status {
  margin-top: 2rpx;
  font-size: 20rpx;
  color: #9aa4b2;
  white-space: nowrap;
}

.stage-active .stage-name {
  font-weight: 600;
  color: #ffffff;
}

.stage-active .stage-status {
  color: rgba(255, 255, 255, 0.86);
}

.stage-arrow {
  flex-shrink: 0;
  padding: 0 6rpx;
  font-size: 26rpx;
  color: #c3cad6;
}

.actions-card {
  display: flex;
  padding: 28rpx 12rpx;
}

.action-item {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
}

.action-icon {
  width: 76rpx;
  height: 76rpx;
}

.action-label {
  margin-top: 12rpx;
  font-size: 25rpx;
  color: #1f2d3d;
}

.news-list {
  margin-top: 22rpx;
}

.news-item {
  display: flex;
  align-items: flex-start;
  margin-top: 22rpx;
}

.news-item:first-child {
  margin-top: 0;
}

.news-img {
  flex-shrink: 0;
  width: 148rpx;
  height: 98rpx;
  border-radius: 10rpx;
}

.news-main {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  margin-left: 18rpx;
}

.news-title {
  font-size: 26rpx;
  font-weight: 600;
  line-height: 1.4;
  color: #1f2d3d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-desc {
  margin-top: 8rpx;
  font-size: 23rpx;
  line-height: 1.5;
  color: #8b95a4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.news-date {
  flex-shrink: 0;
  margin-left: 16rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}
</style>
