<template>
  <view v-if="project" class="page">
    <view class="card head-card">
      <image class="cover" :src="coverImage(project.id)" mode="aspectFill" />
      <view class="head-main">
        <view class="title-row">
          <text class="title">{{ project.name }}</text>
          <text class="status" :class="statusClass(project.status)">{{ statusText(project.status) }}</text>
        </view>
        <view class="info-row">
          <text class="info-icon">▤</text>
          <text class="info-text">计划时间：{{ formatPlan(project) }}</text>
        </view>
        <view class="info-row">
          <text class="info-icon">▣</text>
          <text class="info-text">施工单位：{{ project.constructionUnit || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-icon">☺</text>
          <text class="info-text">负责人：{{ project.manager || '-' }}</text>
        </view>
        <view class="progress-row">
          <text class="progress-label">总体进度</text>
          <view class="track">
            <view class="fill" :style="{ width: project.progress + '%' }" />
          </view>
          <text class="percent">{{ project.progress }}%</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="section-head">
        <image class="section-icon" src="/static/home/icon-layers.png" mode="aspectFit" />
        <text class="section-title">改造内容</text>
      </view>
      <view class="content-grid">
        <view v-for="item in constructionItems" :key="item.name" class="content-item">
          <image class="content-icon" :src="item.icon" mode="aspectFit" />
          <text class="content-name">{{ item.name }}</text>
          <text class="content-desc">{{ item.desc }}</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="section-head">
        <image class="section-icon" src="/static/home/icon-chart.png" mode="aspectFit" />
        <text class="section-title">工程进度</text>
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

    <view class="card">
      <view class="section-head between">
        <view class="section-head">
          <image class="section-icon" src="/static/home/icon-docsearch.png" mode="aspectFit" />
          <text class="section-title">现场照片</text>
        </view>
        <text v-if="allPhotos.length > 3" class="more" @click="previewAll">查看更多 ›</text>
      </view>
      <view class="photo-grid">
        <view v-for="(photo, index) in visiblePhotos" :key="photo.url" class="photo-item">
          <image class="photo" :src="photo.url" mode="aspectFill" @click="previewPhoto(index)" />
          <text class="photo-caption">{{ photo.caption }}</text>
        </view>
      </view>
    </view>

    <view class="action-bar">
      <view class="action-btn feedback" @click="goComplaint">
        <text class="action-icon">💬</text>
        <text>投诉建议</text>
      </view>
      <view class="action-btn notice" @click="goAnnouncements">
        <text class="action-icon">📢</text>
        <text>查看公告</text>
      </view>
    </view>
  </view>

  <view v-else-if="!loading" class="empty-page">
    <text class="empty">项目不存在</text>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchProjectDetail, fetchProjectProgress } from '@/api/app';
import type { AppProgress, AppProject } from '@/api/app';
import { downloadAuthImage } from '@/utils/file';

interface PhotoItem {
  url: string;
  caption: string;
}

const projectImages = [
  '/static/home/project-street.png',
  '/static/home/news-road.png',
  '/static/home/news-notice.png',
  '/static/home/banner-scene.png'
];

const fallbackPhotos: PhotoItem[] = [
  { url: '/static/home/project-street.png', caption: '小区改造现场' },
  { url: '/static/home/news-road.png', caption: '道路施工进行中' },
  { url: '/static/home/news-notice.png', caption: '改造公示现场' }
];

const project = ref<AppProject | null>(null);
const progressList = ref<AppProgress[]>([]);
const photos = ref<PhotoItem[]>([]);
const loading = ref(false);

const constructionItems = [
  {
    name: '道路翻新',
    desc: '对小区主干道进行翻新升级，提升通行舒适性与安全性。',
    icon: '/static/home/icon-docsearch.png'
  },
  {
    name: '雨污分流',
    desc: '改造小区雨污管网，实现雨污分流，减少积水。',
    icon: '/static/home/icon-layers.png'
  },
  {
    name: '照明升级',
    desc: '更换节能 LED 路灯，优化照明布局，提升夜间出行安全。',
    icon: '/static/home/icon-chart.png'
  }
];

const allPhotos = computed(() => (photos.value.length ? photos.value : fallbackPhotos));
const visiblePhotos = computed(() => allPhotos.value.slice(0, 3));

const stages = computed(() => {
  const status = project.value?.status;

  if (status === 0) {
    return [
      { name: '准备阶段', status: '进行中', icon: '/static/home/icon-helmet-green.png', active: true },
      { name: '施工阶段', status: '待启动', icon: '/static/home/icon-check-gray.png', active: false },
      { name: '验收阶段', status: '待启动', icon: '/static/home/icon-flag-gray.png', active: false }
    ];
  }

  if (status === 2) {
    return [
      { name: '准备阶段', status: '已完成', icon: '/static/home/icon-check-gray.png', active: false },
      { name: '施工阶段', status: '已完成', icon: '/static/home/icon-check-gray.png', active: false },
      { name: '验收阶段', status: '已完成', icon: '/static/home/icon-flag-gray.png', active: true }
    ];
  }

  return [
    { name: '准备阶段', status: '已完成', icon: '/static/home/icon-check-gray.png', active: false },
    { name: '施工阶段', status: '进行中', icon: '/static/home/icon-helmet-green.png', active: true },
    { name: '验收阶段', status: '待启动', icon: '/static/home/icon-flag-gray.png', active: false }
  ];
});

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

function previewPhoto(index: number) {
  uni.previewImage({ urls: allPhotos.value.map(item => item.url), current: allPhotos.value[index]?.url });
}

function previewAll() {
  if (!allPhotos.value.length) {
    return;
  }

  previewPhoto(0);
}

function goComplaint() {
  uni.reLaunch({ url: `/pages/complaint/index?projectId=${project.value?.id ?? ''}` });
}

function goAnnouncements() {
  uni.navigateTo({ url: '/pages/announcement/list' });
}

onLoad(async options => {
  const id = Number(options?.id);

  if (!id) {
    return;
  }

  loading.value = true;

  try {
    project.value = await fetchProjectDetail(id);
    const progressPage = await fetchProjectProgress(id, { pageNum: 1, pageSize: 10 });
    progressList.value = progressPage.list;

    const photoItems: PhotoItem[] = [];

    for (const item of progressPage.list) {
      for (const image of item.images) {
        try {
          const url = await downloadAuthImage(image.fileId);
          photoItems.push({ url, caption: item.title });
        } catch {
          // 单张图片失败不影响其他内容
        }
      }
    }

    photos.value = photoItems;
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
  padding: 24rpx 24rpx 200rpx;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.card {
  margin-bottom: 24rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.head-card {
  padding: 0;
  overflow: hidden;
}

.cover {
  width: 100%;
  height: 280rpx;
}

.head-main {
  padding: 24rpx;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 1.4;
  color: #1f2d3d;
}

.status {
  flex-shrink: 0;
  margin-left: 12rpx;
  padding: 4rpx 14rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
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
  margin-top: 12rpx;
}

.info-icon {
  margin-right: 6rpx;
  font-size: 21rpx;
  color: #9aa4b2;
}

.info-text {
  font-size: 23rpx;
  color: #7b8794;
}

.progress-row {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}

.progress-label {
  flex-shrink: 0;
  margin-right: 12rpx;
  font-size: 23rpx;
  color: #6b7280;
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
  margin-left: 12rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #16a34a;
}

.section-head {
  display: flex;
  align-items: center;
}

.section-head.between {
  justify-content: space-between;
}

.section-icon {
  width: 34rpx;
  height: 34rpx;
  margin-right: 10rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.more {
  font-size: 22rpx;
  color: #1677ff;
}

.content-grid {
  display: flex;
  margin-top: 20rpx;
}

.content-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 10rpx;
}

.content-icon {
  width: 56rpx;
  height: 56rpx;
}

.content-name {
  margin-top: 10rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.content-desc {
  margin-top: 8rpx;
  font-size: 20rpx;
  line-height: 1.5;
  text-align: center;
  color: #9aa4b2;
}

.stage-row {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
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
}

.stage-icon {
  flex-shrink: 0;
  width: 34rpx;
  height: 34rpx;
  margin-right: 8rpx;
}

.stage-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stage-name {
  font-size: 22rpx;
  color: #4b5563;
  white-space: nowrap;
}

.stage-status {
  margin-top: 2rpx;
  font-size: 19rpx;
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
  font-size: 24rpx;
  color: #c3cad6;
}

.photo-grid {
  display: flex;
  margin-top: 20rpx;
}

.photo-item {
  flex: 1;
  margin-right: 14rpx;
}

.photo-item:last-child {
  margin-right: 0;
}

.photo {
  width: 100%;
  height: 150rpx;
  border-radius: 12rpx;
}

.photo-caption {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #8b95a4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
  background-color: #ffffff;
  box-shadow: 0 -6rpx 20rpx rgba(31, 45, 61, 0.06);
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 84rpx;
  border-radius: 42rpx;
  font-size: 27rpx;
}

.action-icon {
  margin-right: 8rpx;
  font-size: 26rpx;
}

.action-btn.feedback {
  margin-right: 20rpx;
  color: #1677ff;
  background-color: #ffffff;
  border: 2rpx solid #dbeafe;
}

.action-btn.notice {
  color: #ffffff;
  background: linear-gradient(135deg, #2fb47a 0%, #22a06b 100%);
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
