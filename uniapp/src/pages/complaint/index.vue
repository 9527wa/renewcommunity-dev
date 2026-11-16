<template>
  <view class="page">
    <view class="hero">
      <view class="hero-top">
        <view>
          <text class="hero-title">投诉建议</text>
          <text class="hero-sub">反映小区管理问题、提出建议意见或政策咨询</text>
        </view>
        <text class="hero-link" @click="goMyComplaints">我的投诉 ›</text>
      </view>
    </view>

    <view class="content">
      <view class="card">
        <view class="card-head">
          <image class="head-icon" src="/static/home/icon-chat.png" mode="aspectFit" />
          <view class="head-text">
            <text class="card-title">提交投诉建议</text>
            <text class="card-sub">请如实填写，便于工作人员与您核实办理</text>
          </view>
        </view>

        <view class="form-item">
          <text class="label"><text class="required">*</text>诉求类型</text>
          <view class="kind-tabs">
            <view
              v-for="item in kinds"
              :key="item.label"
              class="kind-tab"
              :class="{ active: form.kind === item.label }"
              @click="form.kind = item.label"
            >
              <image class="kind-icon" :src="item.icon" mode="aspectFit" />
              <text class="kind-label">{{ item.label }}</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="label"><text class="required">*</text>所属小区</text>
          <picker :range="communityNames" @change="onCommunityChange">
            <view class="picker">
              <text>{{ form.community || '请选择小区' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">关联项目（可选）</text>
          <picker :range="projectLabels" @change="onProjectChange">
            <view class="picker">
              <text>{{ projectLabel }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label"><text class="required">*</text>问题位置</text>
          <input v-model="form.location" class="input" maxlength="200" placeholder="如 3 号楼 2 单元楼道" />
        </view>

        <view class="form-item">
          <text class="label"><text class="required">*</text>诉求标题</text>
          <input v-model="form.title" class="input" maxlength="100" placeholder="请简要描述您的诉求" />
        </view>

        <view class="form-item">
          <text class="label"><text class="required">*</text>问题描述</text>
          <textarea
            v-model="form.content"
            class="textarea"
            maxlength="500"
            placeholder="请详细描述问题情况，便于工作人员核实处理"
          />
          <text class="counter">{{ form.content.length }}/500</text>
        </view>

        <view class="form-item">
          <text class="label"><text class="required">*</text>联系电话</text>
          <input
            v-model="form.contactMobile"
            class="input"
            type="number"
            maxlength="20"
            placeholder="请输入联系电话"
          />
        </view>

        <view class="form-item">
          <view class="label-row">
            <text class="label">上传现场图片</text>
            <text class="label-tip">最多上传 9 张</text>
          </view>
          <view class="photo-grid">
            <view v-for="(photo, index) in images" :key="photo" class="photo-item">
              <image class="photo" :src="photo" mode="aspectFill" @click="previewImage(index)" />
              <text class="photo-remove" @click="removeImage(index)">×</text>
            </view>
            <view v-if="images.length < 9" class="photo-add" @click="chooseImages">
              <text class="photo-add-icon">＋</text>
              <text class="photo-add-text">上传图片</text>
            </view>
          </view>
        </view>

        <view class="tip-box">
          <text class="tip-icon">i</text>
          <text class="tip-text">
            温馨提示：请如实填写诉求内容，客观描述问题情况，并保持联系电话畅通，以便工作人员及时与您核实、办理反馈。
          </text>
        </view>

        <button class="submit-btn" :loading="submitting" :disabled="submitting" @click="handleSubmit">
          提交诉求
        </button>
      </view>
    </view>

    <tab-bar active="complaint" />
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import {
  fetchCommunityList,
  fetchProfile,
  fetchProjectList,
  submitComplaint,
  uploadComplaintImage
} from '@/api/app';
import type { AppCommunity, AppProfile, AppProject } from '@/api/app';
import TabBar from '@/components/tab-bar/tab-bar.vue';
import { clearToken, isLoggedIn } from '@/utils/auth';

interface PickerEvent {
  detail: {
    value: string;
  };
}

const kinds = [
  { label: '投诉', icon: '/static/home/icon-chat.png' },
  { label: '建议', icon: '/static/home/icon-clipboard.png' },
  { label: '咨询', icon: '/static/home/icon-docsearch.png' }
];

const form = reactive({
  kind: '投诉',
  community: '',
  projectId: null as number | null,
  location: '',
  title: '',
  content: '',
  contactMobile: ''
});

const profile = ref<AppProfile | null>(null);
const communities = ref<AppCommunity[]>([]);
const projects = ref<AppProject[]>([]);
const images = ref<string[]>([]);
const submitting = ref(false);
let pendingProjectId: number | null = null;

const communityNames = computed(() => communities.value.map(item => item.name));

const projectChoices = computed(() => [
  { id: 0, name: '不关联项目', community: '' },
  ...projects.value.map(item => ({ id: item.id, name: item.name, community: item.community }))
]);

const projectLabels = computed(() => projectChoices.value.map(item => item.name));
const projectLabel = computed(
  () => projectChoices.value.find(item => item.id === (form.projectId ?? 0))?.name ?? '不关联项目'
);

function redirectToLogin() {
  clearToken();
  uni.reLaunch({ url: '/pages/login/login' });
}

async function loadBaseData() {
  try {
    const [profileData, communityList, projectPage] = await Promise.all([
      fetchProfile(),
      fetchCommunityList(),
      fetchProjectList({ pageNum: 1, pageSize: 100 })
    ]);

    profile.value = profileData;
    communities.value = communityList;
    projects.value = projectPage.list;

    if (!form.community) {
      form.community = profileData.communityName || '';
    }

    if (!form.contactMobile) {
      form.contactMobile = profileData.mobile || '';
    }

    if (pendingProjectId) {
      const target = projects.value.find(item => item.id === pendingProjectId);

      if (target) {
        form.projectId = target.id;
        form.community = target.community || form.community;
      }

      pendingProjectId = null;
    }
  } catch {
    // 错误提示由 request 统一处理
  }
}

function onCommunityChange(event: PickerEvent) {
  form.community = communityNames.value[Number(event.detail.value)] ?? '';
}

function onProjectChange(event: PickerEvent) {
  const choice = projectChoices.value[Number(event.detail.value)];

  if (!choice) {
    return;
  }

  form.projectId = choice.id || null;

  if (!form.community && choice.community) {
    form.community = choice.community;
  }
}

function chooseImages() {
  const remaining = 9 - images.value.length;

  if (remaining <= 0) {
    return;
  }

  uni.chooseImage({
    count: remaining,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: res => {
      images.value = [...images.value, ...res.tempFilePaths.slice(0, remaining)];
    }
  });
}

function removeImage(index: number) {
  images.value = images.value.filter((_, i) => i !== index);
}

function previewImage(index: number) {
  uni.previewImage({ urls: images.value, current: images.value[index] });
}

function goMyComplaints() {
  uni.navigateTo({ url: '/pages/complaint/my' });
}

async function handleSubmit() {
  if (!form.community) {
    uni.showToast({ title: '请选择所属小区', icon: 'none' });
    return;
  }

  if (!form.location.trim()) {
    uni.showToast({ title: '请输入问题位置', icon: 'none' });
    return;
  }

  if (!form.title.trim()) {
    uni.showToast({ title: '请输入诉求标题', icon: 'none' });
    return;
  }

  if (!form.content.trim()) {
    uni.showToast({ title: '请输入问题描述', icon: 'none' });
    return;
  }

  if (!form.contactMobile.trim()) {
    uni.showToast({ title: '请输入联系电话', icon: 'none' });
    return;
  }

  submitting.value = true;

  try {
    const complaintId = await submitComplaint({
      title: form.title.trim(),
      content: form.content.trim(),
      kind: form.kind,
      type: '其他',
      location: form.location.trim(),
      projectId: form.projectId,
      community: form.community,
      contactName: profile.value?.userName,
      contactMobile: form.contactMobile.trim()
    });

    let uploadFailed = false;

    for (const path of images.value) {
      try {
        await uploadComplaintImage(complaintId, path);
      } catch {
        uploadFailed = true;
      }
    }

    uni.showToast({
      title: uploadFailed ? '诉求已提交，部分图片上传失败' : '提交成功',
      icon: uploadFailed ? 'none' : 'success'
    });

    form.title = '';
    form.content = '';
    form.location = '';
    images.value = [];
    await loadBaseData();
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    submitting.value = false;
  }
}

onLoad(options => {
  pendingProjectId = Number(options?.projectId) || null;
});

onShow(() => {
  if (!isLoggedIn()) {
    redirectToLogin();
    return;
  }

  loadBaseData();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.hero {
  padding: calc(36rpx + var(--status-bar-height)) 32rpx 64rpx;
  background: linear-gradient(180deg, #3d92ef 0%, #5aa6f3 55%, #86c0f9 100%);
}

.hero-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.hero-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #ffffff;
}

.hero-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.86);
}

.hero-link {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.92);
}

.content {
  margin-top: -32rpx;
  padding: 0 24rpx 200rpx;
}

.card {
  padding: 28rpx 24rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.card-head {
  display: flex;
  align-items: center;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f1f4f8;
}

.head-icon {
  width: 44rpx;
  height: 44rpx;
  margin-right: 14rpx;
}

.head-text {
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.card-sub {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-top: 24rpx;
}

.label {
  margin-bottom: 10rpx;
  font-size: 26rpx;
  color: #48505e;
}

.required {
  margin-right: 4rpx;
  color: #e5484d;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.label-tip {
  font-size: 22rpx;
  color: #9aa4b2;
}

.kind-tabs {
  display: flex;
}

.kind-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 72rpx;
  margin-right: 14rpx;
  border: 2rpx solid #e4e8ef;
  border-radius: 12rpx;
  background-color: #f8fafc;
}

.kind-tab:last-child {
  margin-right: 0;
}

.kind-tab.active {
  border-color: #22a06b;
  background-color: #e9f8ef;
}

.kind-icon {
  width: 30rpx;
  height: 30rpx;
  margin-right: 8rpx;
}

.kind-label {
  font-size: 25rpx;
  color: #5b6472;
}

.kind-tab.active .kind-label {
  color: #15803d;
  font-weight: 600;
}

.input,
.picker,
.textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 0 22rpx;
  font-size: 26rpx;
  background-color: #f5f7fa;
  border: 2rpx solid #e4e8ef;
  border-radius: 12rpx;
}

.input {
  height: 84rpx;
}

.picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 84rpx;
  color: #1f2d3d;
}

.picker-arrow {
  font-size: 28rpx;
  color: #9aa4b2;
}

.textarea {
  height: 220rpx;
  padding: 18rpx 22rpx;
  line-height: 1.6;
}

.counter {
  margin-top: 6rpx;
  text-align: right;
  font-size: 20rpx;
  color: #9aa4b2;
}

.photo-grid {
  display: flex;
  flex-wrap: wrap;
}

.photo-item {
  position: relative;
  margin: 0 16rpx 16rpx 0;
}

.photo {
  width: 150rpx;
  height: 150rpx;
  border-radius: 12rpx;
}

.photo-remove {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 36rpx;
  height: 36rpx;
  line-height: 32rpx;
  border-radius: 50%;
  background-color: rgba(15, 23, 42, 0.65);
  color: #ffffff;
  font-size: 26rpx;
  text-align: center;
}

.photo-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 150rpx;
  height: 150rpx;
  border: 2rpx dashed #d7dde7;
  border-radius: 12rpx;
  background-color: #f8fafc;
}

.photo-add-icon {
  font-size: 36rpx;
  color: #9aa4b2;
}

.photo-add-text {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #9aa4b2;
}

.tip-box {
  display: flex;
  margin-top: 28rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  background-color: #f0f7ff;
}

.tip-icon {
  flex-shrink: 0;
  width: 30rpx;
  height: 30rpx;
  margin-right: 10rpx;
  border-radius: 50%;
  background-color: #3b9cf7;
  color: #ffffff;
  font-size: 20rpx;
  line-height: 30rpx;
  text-align: center;
  font-style: normal;
}

.tip-text {
  font-size: 23rpx;
  line-height: 1.65;
  color: #5b7ba6;
}

.submit-btn {
  width: 100%;
  height: 90rpx;
  line-height: 90rpx;
  margin-top: 34rpx;
  font-size: 30rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #3b9cf7 0%, #1677ff 100%);
  border-radius: 45rpx;
  box-shadow: 0 8rpx 20rpx rgba(22, 119, 255, 0.24);
}
</style>
