<template>
  <view class="page">
    <view class="card">
      <text class="hello">你好，{{ userInfo.userName || '用户' }}</text>
      <text class="role">角色：{{ roleText }}</text>
    </view>

    <view class="tips">
      <text>小程序端正在建设中，后续将提供项目台账查询、资料上传与进度填报等功能。</text>
    </view>

    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getUserInfo, logout } from '@/api/auth';
import type { UserInfo } from '@/api/auth';
import { clearToken, isLoggedIn } from '@/utils/auth';

const userInfo = reactive<UserInfo>({
  userId: '',
  userName: '',
  roles: [],
  buttons: []
});

const roleText = computed(() => (userInfo.roles.length ? userInfo.roles.join('、') : '无'));

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
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 40rpx;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.card {
  display: flex;
  flex-direction: column;
  padding: 48rpx 40rpx;
  background-color: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(31, 45, 61, 0.08);
}

.hello {
  font-size: 40rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.role {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #7b8794;
}

.tips {
  margin-top: 32rpx;
  padding: 32rpx;
  font-size: 28rpx;
  line-height: 1.7;
  color: #7b8794;
  background-color: #ffffff;
  border-radius: 24rpx;
}

.logout-btn {
  margin-top: 64rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  color: #e5484d;
  background-color: #ffffff;
  border: 2rpx solid #f2c4c6;
  border-radius: 12rpx;
}
</style>
