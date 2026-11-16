<template>
  <view class="page">
    <view class="hero">
      <view class="hero-main">
        <view class="avatar">{{ avatarText }}</view>
        <view class="hero-info">
          <view class="name-row">
            <text class="name">{{ profile?.userName || '用户' }}</text>
            <text class="role-tag">{{ roleText }}</text>
          </view>
          <view class="community-row" @click="switchCommunity">
            <text class="pin">📍</text>
            <text class="community">{{ profile?.communityName || '未绑定小区' }}</text>
            <text class="chevron">›</text>
          </view>
          <text class="welcome">欢迎使用老旧小区改造服务平台</text>
        </view>
      </view>
    </view>

    <view class="content">
      <view class="stats-card">
        <view class="stat-item">
          <view class="stat-icon blue">📝</view>
          <view class="stat-text">
            <text class="stat-value">{{ profile?.complaintTotal ?? 0 }}</text>
            <text class="stat-label">提交诉求</text>
          </view>
        </view>
        <view class="stat-item">
          <view class="stat-icon green">🛠</view>
          <view class="stat-text">
            <text class="stat-value green">{{ profile?.complaintProcessing ?? 0 }}</text>
            <text class="stat-label">处理中</text>
          </view>
        </view>
        <view class="stat-item">
          <view class="stat-icon teal">✅</view>
          <view class="stat-text">
            <text class="stat-value">{{ profile?.complaintResolved ?? 0 }}</text>
            <text class="stat-label">已办结</text>
          </view>
        </view>
      </view>

      <view class="card menu-card">
        <view class="menu-row" @click="goMyComplaints">
          <image class="menu-icon" src="/static/home/icon-chat.png" mode="aspectFit" />
          <text class="menu-label">我的投诉</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-row" @click="goProcessing">
          <image class="menu-icon" src="/static/home/icon-clipboard.png" mode="aspectFit" />
          <text class="menu-label">办理进度</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-row" @click="goAnnouncements">
          <image class="menu-icon" src="/static/home/icon-megaphone.png" mode="aspectFit" />
          <text class="menu-label">公告通知</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-row" @click="goRatings">
          <view class="menu-icon emoji-icon star">★</view>
          <text class="menu-label">我的评价</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-row" @click="switchCommunity">
          <view class="menu-icon emoji-icon building">🏢</view>
          <text class="menu-label">切换小区</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-row" @click="goSettings">
          <view class="menu-icon emoji-icon setting">⚙</view>
          <text class="menu-label">设置</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="card help-card">
        <text class="help-title">常用帮助</text>
        <view class="help-row">
          <view class="help-item" @click="showHelp('平台说明')">
            <image class="help-icon" src="/static/home/icon-docsearch.png" mode="aspectFit" />
            <text class="help-label">平台说明</text>
          </view>
          <view class="help-item" @click="showHelp('联系我们')">
            <image class="help-icon" src="/static/home/icon-chat.png" mode="aspectFit" />
            <text class="help-label">联系我们</text>
          </view>
          <view class="help-item" @click="showHelp('隐私政策')">
            <image class="help-icon" src="/static/home/icon-clipboard.png" mode="aspectFit" />
            <text class="help-label">隐私政策</text>
          </view>
        </view>
      </view>
    </view>

    <tab-bar active="profile" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { bindCommunity, fetchCommunityList, fetchProfile } from '@/api/app';
import type { AppCommunity, AppProfile } from '@/api/app';
import TabBar from '@/components/tab-bar/tab-bar.vue';
import { clearToken, isLoggedIn } from '@/utils/auth';
import { getRoleText } from '@/utils/role';

const profile = ref<AppProfile | null>(null);
const communities = ref<AppCommunity[]>([]);

const roleText = computed(() => getRoleText(profile.value?.roles ?? []));
const avatarText = computed(() => (profile.value?.userName || '用').slice(0, 1));

function redirectToLogin() {
  clearToken();
  uni.reLaunch({ url: '/pages/login/login' });
}

async function loadProfile() {
  try {
    const [profileData, communityList] = await Promise.all([fetchProfile(), fetchCommunityList()]);
    profile.value = profileData;
    communities.value = communityList;
  } catch {
    redirectToLogin();
  }
}

onShow(() => {
  if (!isLoggedIn()) {
    redirectToLogin();
    return;
  }

  loadProfile();
});

function switchCommunity() {
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

function goMyComplaints() {
  uni.navigateTo({ url: '/pages/complaint/my' });
}

function goProcessing() {
  uni.navigateTo({ url: '/pages/complaint/my?status=1' });
}

function goAnnouncements() {
  uni.navigateTo({ url: '/pages/announcement/list' });
}

function goRatings() {
  uni.navigateTo({ url: '/pages/profile/ratings' });
}

function goSettings() {
  uni.navigateTo({ url: '/pages/profile/settings' });
}

function showHelp(name: string) {
  const contents: Record<string, string> = {
    平台说明: '本平台用于老旧小区改造项目资料公示、进度查询与居民诉求反馈。',
    联系我们: '如需帮助，请联系社区服务中心：138-0000-0000。',
    隐私政策: '我们仅在办理业务所需范围内使用您的个人信息，不会向无关第三方提供。'
  };

  uni.showModal({
    title: name,
    content: contents[name] ?? '',
    showCancel: false
  });
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.hero {
  padding: calc(30rpx + var(--status-bar-height)) 32rpx 60rpx;
  background: linear-gradient(180deg, #3d92ef 0%, #5aa6f3 55%, #86c0f9 100%);
}

.hero-main {
  display: flex;
  align-items: center;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.92);
  font-size: 46rpx;
  font-weight: 700;
  color: #1677ff;
  box-shadow: 0 8rpx 20rpx rgba(12, 74, 138, 0.18);
}

.hero-info {
  flex: 1;
  margin-left: 24rpx;
}

.name-row {
  display: flex;
  align-items: center;
}

.name {
  font-size: 38rpx;
  font-weight: 700;
  color: #ffffff;
}

.role-tag {
  margin-left: 12rpx;
  padding: 2rpx 12rpx;
  border-radius: 12rpx;
  background-color: rgba(255, 255, 255, 0.22);
  font-size: 20rpx;
  color: #ffffff;
}

.community-row {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
}

.pin {
  margin-right: 4rpx;
  font-size: 20rpx;
}

.community {
  font-size: 23rpx;
  color: rgba(255, 255, 255, 0.92);
}

.chevron {
  margin-left: 6rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}

.welcome {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.78);
}

.content {
  margin-top: -36rpx;
  padding: 0 24rpx 200rpx;
}

.stats-card {
  display: flex;
  padding: 26rpx 10rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.stat-item {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
}

.stat-item + .stat-item {
  border-left: 1rpx solid #f1f4f8;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  margin-right: 10rpx;
  border-radius: 14rpx;
  font-size: 26rpx;
}

.stat-icon.blue {
  background-color: #eaf3ff;
}

.stat-icon.green {
  background-color: #e9f8ef;
}

.stat-icon.teal {
  background-color: #e6f7f3;
}

.stat-text {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.stat-value.green {
  color: #16a34a;
}

.stat-label {
  margin-top: 2rpx;
  font-size: 21rpx;
  color: #8b95a4;
}

.card {
  margin-top: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.menu-row {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f1f4f8;
}

.menu-row:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 52rpx;
  height: 52rpx;
  margin-right: 18rpx;
}

.emoji-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14rpx;
  font-size: 28rpx;
}

.emoji-icon.star {
  background-color: #fff7e6;
  color: #f59e0b;
}

.emoji-icon.building {
  background-color: #eaf3ff;
}

.emoji-icon.setting {
  background-color: #f1f4f8;
  color: #5b6472;
}

.menu-label {
  flex: 1;
  font-size: 27rpx;
  color: #1f2d3d;
}

.menu-arrow {
  font-size: 30rpx;
  color: #c3cad6;
}

.help-card {
  padding: 24rpx;
}

.help-title {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.help-row {
  display: flex;
  margin-top: 20rpx;
}

.help-item {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 8rpx;
  margin-right: 14rpx;
  border-radius: 14rpx;
  background-color: #f8fafc;
}

.help-item:last-child {
  margin-right: 0;
}

.help-icon {
  width: 42rpx;
  height: 42rpx;
}

.help-label {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #5b6472;
}
</style>
