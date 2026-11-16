<template>
  <view class="page">
    <view class="card">
      <text class="card-title">个人资料</text>
      <view class="form-item">
        <text class="label">昵称</text>
        <input v-model="model.nickname" class="input" maxlength="50" placeholder="请输入昵称" />
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="model.mobile" class="input" type="number" maxlength="20" placeholder="请输入手机号" />
      </view>
      <view class="form-item">
        <text class="label">邮箱</text>
        <input v-model="model.email" class="input" maxlength="100" placeholder="请输入邮箱" />
      </view>
      <button class="save-btn" :loading="saving" :disabled="saving" @click="saveProfile">保存资料</button>
    </view>

    <view class="card">
      <view class="row">
        <text class="row-label">消息通知</text>
        <switch :checked="notify" color="#22a06b" @change="onNotifyChange" />
      </view>
      <view class="row" @click="clearCache">
        <text class="row-label">清除缓存</text>
        <text class="row-arrow">›</text>
      </view>
      <view class="row" @click="showAbout">
        <text class="row-label">关于平台</text>
        <text class="row-value">v1.0.0</text>
      </view>
      <view class="row" @click="showPrivacy">
        <text class="row-label">隐私政策</text>
        <text class="row-arrow">›</text>
      </view>
    </view>

    <button class="logout-btn" @click="handleLogout">退出登录</button>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app';
import { reactive, ref } from 'vue';
import { fetchProfile, updateProfile } from '@/api/app';
import { logout } from '@/api/auth';
import { clearToken, isLoggedIn } from '@/utils/auth';

const model = reactive({
  nickname: '',
  mobile: '',
  email: ''
});

const saving = ref(false);
const notify = ref(true);

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
    const profile = await fetchProfile();
    model.nickname = profile.userName;
    model.mobile = profile.mobile;
    model.email = profile.email;
  } catch {
    redirectToLogin();
  }
});

async function saveProfile() {
  if (!model.nickname.trim()) {
    uni.showToast({ title: '昵称不能为空', icon: 'none' });
    return;
  }

  saving.value = true;

  try {
    await updateProfile({
      nickname: model.nickname.trim(),
      mobile: model.mobile.trim(),
      email: model.email.trim()
    });
    uni.showToast({ title: '资料已保存', icon: 'success' });
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    saving.value = false;
  }
}

function onNotifyChange(event: Event) {
  const detail = (event as unknown as { detail?: { value?: boolean } }).detail;

  if (typeof detail?.value === 'boolean') {
    notify.value = detail.value;
  }
}

function clearCache() {
  try {
    const info = uni.getStorageInfoSync();
    info.keys
      .filter(key => key !== 'renew_token')
      .forEach(key => uni.removeStorageSync(key));
    uni.showToast({ title: '缓存已清除', icon: 'success' });
  } catch {
    uni.showToast({ title: '缓存清理失败', icon: 'none' });
  }
}

function showAbout() {
  uni.showModal({
    title: '关于平台',
    content: '老旧小区改造资料台账服务平台 v1.0.0\n提供项目公示、进度查询、诉求反馈等服务。',
    showCancel: false
  });
}

function showPrivacy() {
  uni.showModal({
    title: '隐私政策',
    content: '我们仅在办理业务所需范围内使用您的个人信息，不会向无关第三方提供。',
    showCancel: false
  });
}

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
  padding: 24rpx;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.card {
  margin-bottom: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.card-title {
  display: block;
  padding: 24rpx 0 8rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-top: 20rpx;
}

.label {
  margin-bottom: 10rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.input {
  height: 82rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  background-color: #f5f7fa;
  border: 2rpx solid #e4e8ef;
  border-radius: 12rpx;
}

.save-btn {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  margin: 28rpx 0 24rpx;
  font-size: 27rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #2fb47a 0%, #22a06b 100%);
  border-radius: 42rpx;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f1f4f8;
}

.row:last-child {
  border-bottom: none;
}

.row-label {
  font-size: 27rpx;
  color: #1f2d3d;
}

.row-value {
  font-size: 24rpx;
  color: #9aa4b2;
}

.row-arrow {
  font-size: 30rpx;
  color: #c3cad6;
}

.logout-btn {
  width: 100%;
  height: 92rpx;
  line-height: 92rpx;
  margin-top: 16rpx;
  font-size: 29rpx;
  color: #e5484d;
  background-color: #ffffff;
  border: 2rpx solid #f2c4c6;
  border-radius: 16rpx;
}

.logout-btn::after {
  border: none;
}
</style>
