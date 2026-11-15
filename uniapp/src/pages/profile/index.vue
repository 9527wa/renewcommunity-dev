<template>
  <view class="page">
    <view class="profile-header">
      <view class="avatar">{{ avatarText }}</view>
      <view class="profile-info">
        <text class="name">{{ userInfo.userName || '用户' }}</text>
        <text class="role">{{ roleText }}</text>
      </view>
    </view>

    <view class="content">
      <view class="card">
        <view class="row">
          <text class="row-label">账号 ID</text>
          <text class="row-value">{{ userInfo.userId || '-' }}</text>
        </view>
        <view class="row">
          <text class="row-label">角色</text>
          <text class="row-value">{{ roleText }}</text>
        </view>
        <view class="row">
          <text class="row-label">可用权限</text>
          <text class="row-value">{{ userInfo.buttons.length }} 项</text>
        </view>
      </view>

      <view class="card links-card">
        <view class="row link-row" @click="goComplaint">
          <text class="row-label">我的投诉</text>
          <text class="chevron">›</text>
        </view>
        <view class="row link-row" @click="goAnnouncements">
          <text class="row-label">公告通知</text>
          <text class="chevron">›</text>
        </view>
        <view class="row link-row" @click="goProjects">
          <text class="row-label">改造项目</text>
          <text class="chevron">›</text>
        </view>
      </view>

      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <tab-bar active="profile" />
  </view>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getUserInfo, logout } from '@/api/auth';
import type { UserInfo } from '@/api/auth';
import { clearToken, isLoggedIn } from '@/utils/auth';
import { getRoleText } from '@/utils/role';
import TabBar from '@/components/tab-bar/tab-bar.vue';

const userInfo = reactive<UserInfo>({
  userId: '',
  userName: '',
  roles: [],
  buttons: []
});

const roleText = computed(() => getRoleText(userInfo.roles));

const avatarText = computed(() => (userInfo.userName || '用').slice(0, 1));

function redirectToLogin() {
  clearToken();
  uni.reLaunch({ url: '/pages/login/login' });
}

onShow(async () => {
  if (!isLoggedIn()) {
    redirectToLogin();
    return;
  }

  try {
    Object.assign(userInfo, await getUserInfo());
  } catch {
    redirectToLogin();
  }
});

async function handleLogout() {
  try {
    await logout();
  } catch {
    // 即使后端登出失败，也清理本地登录态
  }
  redirectToLogin();
}

function goComplaint() {
  uni.reLaunch({ url: '/pages/complaint/index' });
}

function goAnnouncements() {
  uni.navigateTo({ url: '/pages/announcement/list' });
}

function goProjects() {
  uni.navigateTo({ url: '/pages/project/list' });
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.profile-header {
  display: flex;
  align-items: center;
  height: 260rpx;
  padding: calc(24rpx + var(--status-bar-height)) 36rpx 40rpx;
  box-sizing: border-box;
  background: linear-gradient(180deg, #3d92ef 0%, #5aa6f3 55%, #86c0f9 100%);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.92);
  font-size: 44rpx;
  font-weight: 700;
  color: #1677ff;
  box-shadow: 0 8rpx 20rpx rgba(12, 74, 138, 0.18);
}

.profile-info {
  display: flex;
  flex-direction: column;
  margin-left: 24rpx;
}

.name {
  font-size: 36rpx;
  font-weight: 700;
  color: #ffffff;
}

.role {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.86);
}

.content {
  margin-top: -48rpx;
  padding: 0 24rpx 200rpx;
}

.card {
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.links-card {
  margin-top: 24rpx;
}

.chevron {
  font-size: 30rpx;
  color: #b0b8c4;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx 0;
  border-bottom: 1rpx solid #f1f4f8;
}

.row:last-child {
  border-bottom: none;
}

.row-label {
  font-size: 27rpx;
  color: #6b7280;
}

.row-value {
  font-size: 27rpx;
  color: #1f2d3d;
}

.logout-btn {
  width: 100%;
  height: 92rpx;
  line-height: 92rpx;
  margin-top: 32rpx;
  font-size: 30rpx;
  color: #e5484d;
  background-color: #ffffff;
  border: 2rpx solid #f2c4c6;
  border-radius: 16rpx;
}

.logout-btn::after {
  border: none;
}
</style>
