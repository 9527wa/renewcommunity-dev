<template>
  <view class="page">
    <view v-if="detail" class="content">
      <view class="card head-card">
        <view class="head-row">
          <text class="title">{{ detail.title }}</text>
          <text class="status" :class="statusClass(detail.status)">{{ statusText(detail.status) }}</text>
        </view>

        <view class="info-row">
          <text class="info-icon">▤</text>
          <text class="info-text">工单编号：{{ detail.orderNo || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-icon">📍</text>
          <text class="info-text">所属小区：{{ detail.community || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-icon">◷</text>
          <text class="info-text">提交时间：{{ formatDate(detail.createdAt) }}</text>
        </view>
      </view>

      <view class="card">
        <view class="section-head">
          <image class="section-icon" src="/static/home/icon-chat.png" mode="aspectFit" />
          <text class="section-title">问题描述</text>
        </view>
        <text class="body">{{ detail.content }}</text>
        <view v-if="photos.length" class="photo-grid">
          <image
            v-for="(photo, index) in photos"
            :key="photo"
            class="photo"
            :src="photo"
            mode="aspectFill"
            @click="previewPhotos(index)"
          />
        </view>
      </view>

      <view class="card">
        <view class="section-head">
          <image class="section-icon" src="/static/home/icon-chart.png" mode="aspectFit" />
          <text class="section-title">办理进度</text>
        </view>

        <view class="timeline">
          <view v-for="(step, index) in steps" :key="step.title" class="timeline-item">
            <view class="timeline-left">
              <view class="dot" :class="{ done: step.done, active: step.active }">
                <text v-if="step.done" class="dot-check">✓</text>
                <text v-else class="dot-num">{{ index + 1 }}</text>
              </view>
              <view v-if="index < steps.length - 1" class="line" :class="{ done: step.done }" />
            </view>
            <view class="timeline-main">
              <view class="timeline-head">
                <text class="timeline-title" :class="{ active: step.active }">{{ step.title }}</text>
                <text class="timeline-time">{{ step.time }}</text>
              </view>
              <text class="timeline-desc">{{ step.desc }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="tip-box">
        <text class="tip-icon">i</text>
        <text class="tip-text">
          温馨提示：办理结果更新后，我们会通过微信消息及时通知您，请注意查看。
        </text>
      </view>

      <view v-if="detail.status === 2" class="card">
        <view class="rate-head">
          <text class="section-title">满意度评价</text>
          <text v-if="detail.rated" class="rated">已评价 {{ detail.score }} 分</text>
          <text v-else class="rate-link" @click="openRating">去评价</text>
        </view>
        <view v-if="detail.rated" class="stars">
          <text v-for="n in 5" :key="n" class="star" :class="{ active: n <= (detail.score || 0) }">★</text>
        </view>
        <text v-else class="rate-hint">诉求已办结，可以对处理结果进行评价</text>
      </view>
    </view>

    <view v-else-if="!loading" class="empty">投诉不存在或无权查看</view>

    <view v-if="detail" class="action-bar">
      <view
        v-if="detail.status !== 2"
        class="action-btn remind"
        @click="handleRemind"
      >
        催办提醒{{ detail.remindCount ? `（${detail.remindCount}）` : '' }}
      </view>
      <view class="action-btn back" @click="goBack">返回列表</view>
    </view>

    <view v-if="ratingVisible" class="rating-mask" @click.self="closeRating">
      <view class="rating-modal">
        <text class="rating-title">评价投诉处理</text>
        <text class="rating-complaint">{{ detail?.title }}</text>
        <view class="stars rate-stars">
          <text
            v-for="n in 5"
            :key="n"
            class="star"
            :class="{ active: n <= ratingScore }"
            @click="ratingScore = n"
          >
            ★
          </text>
        </view>
        <textarea
          v-model="ratingComment"
          class="rating-textarea"
          maxlength="300"
          placeholder="说说处理结果（选填）"
        />
        <view class="rating-actions">
          <view class="rating-cancel" @click="closeRating">取消</view>
          <view class="rating-submit" @click="handleRatingSubmit">
            {{ ratingSubmitting ? '提交中...' : '提交评价' }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchComplaintDetail, remindComplaint, submitSatisfaction } from '@/api/app';
import type { AppComplaint } from '@/api/app';
import { downloadAuthImage } from '@/utils/file';

const detail = ref<AppComplaint | null>(null);
const photos = ref<string[]>([]);
const loading = ref(false);
const ratingVisible = ref(false);
const ratingSubmitting = ref(false);
const ratingScore = ref(5);
const ratingComment = ref('');
let complaintId: number | null = null;

function statusText(status: number) {
  if (status === 0) {
    return '待受理';
  }

  if (status === 1) {
    return '处理中';
  }

  return '已办结';
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

function formatDate(value?: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '';
}

const steps = computed(() => {
  const status = detail.value?.status ?? 0;
  const createdAt = formatDate(detail.value?.createdAt);
  const handledAt = formatDate(detail.value?.handledAt);
  const reply = detail.value?.reply;

  if (status === 2) {
    return [
      { title: '已提交', time: createdAt, desc: '您提交诉求，系统已生成工单。', done: true, active: false },
      { title: '已受理', time: handledAt, desc: '社区工作人员已受理您的诉求。', done: true, active: false },
      { title: '已分派', time: handledAt, desc: '已分派至相关责任单位办理。', done: true, active: false },
      { title: '已办结', time: handledAt, desc: reply || '您的诉求已办结。', done: true, active: true }
    ];
  }

  return [
    { title: '已提交', time: createdAt, desc: '您提交诉求，系统已生成工单。', done: true, active: status === 0 },
    { title: '已受理', time: status >= 1 ? handledAt || createdAt : '', desc: '社区工作人员正在核实情况。', done: status >= 1, active: false },
    { title: '已分派', time: status >= 1 ? handledAt || createdAt : '', desc: '已分派至相关责任单位办理。', done: status >= 1, active: false },
    { title: '处理中', time: status >= 1 ? handledAt || createdAt : '', desc: reply || '工作人员正在处理您的诉求。', done: false, active: status === 1 }
  ];
});

async function load() {
  if (!complaintId) {
    return;
  }

  loading.value = true;

  try {
    const data = await fetchComplaintDetail(complaintId);
    detail.value = data;
    photos.value = (
      await Promise.all(
        (data.images || []).map(async image => {
          try {
            return await downloadAuthImage(image.fileId);
          } catch {
            return '';
          }
        })
      )
    ).filter(Boolean);
  } catch {
    detail.value = null;
    photos.value = [];
  } finally {
    loading.value = false;
  }
}

function previewPhotos(index: number) {
  uni.previewImage({ urls: photos.value, current: photos.value[index] });
}

async function handleRemind() {
  if (!complaintId) {
    return;
  }

  try {
    const count = await remindComplaint(complaintId);

    if (detail.value) {
      detail.value.remindCount = count;
    }

    uni.showToast({ title: `已催办，累计 ${count} 次`, icon: 'none' });
  } catch {
    // 错误提示由 request 统一处理
  }
}

function goBack() {
  uni.navigateBack({
    fail: () => {
      uni.reLaunch({ url: '/pages/complaint/my' });
    }
  });
}

function openRating() {
  ratingScore.value = 5;
  ratingComment.value = '';
  ratingVisible.value = true;
}

function closeRating() {
  ratingVisible.value = false;
}

async function handleRatingSubmit() {
  if (!complaintId) {
    return;
  }

  ratingSubmitting.value = true;

  try {
    await submitSatisfaction({
      complaintId,
      score: ratingScore.value,
      comment: ratingComment.value.trim() || undefined
    });

    uni.showToast({ title: '评价成功', icon: 'success' });
    ratingVisible.value = false;
    await load();
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    ratingSubmitting.value = false;
  }
}

onLoad(options => {
  complaintId = Number(options?.id) || null;
  load();
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
  padding: 28rpx 24rpx;
  border-radius: 20rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.head-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 1.45;
  color: #1f2d3d;
}

.status {
  flex-shrink: 0;
  margin-left: 16rpx;
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

.info-row {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
}

.info-icon {
  width: 30rpx;
  margin-right: 6rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}

.info-text {
  font-size: 23rpx;
  color: #7b8794;
}

.section-head {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
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

.body {
  display: block;
  font-size: 26rpx;
  line-height: 1.7;
  color: #4b5563;
}

.photo-grid {
  display: flex;
  flex-wrap: wrap;
  margin-top: 16rpx;
}

.photo {
  width: 170rpx;
  height: 150rpx;
  margin: 0 14rpx 14rpx 0;
  border-radius: 12rpx;
}

.timeline {
  margin-top: 8rpx;
}

.timeline-item {
  display: flex;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 44rpx;
}

.dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30rpx;
  height: 30rpx;
  margin-top: 6rpx;
  border-radius: 50%;
  background-color: #e2e8f0;
}

.dot.done {
  background-color: #22a06b;
}

.dot.active {
  background-color: #22a06b;
  box-shadow: 0 0 0 6rpx rgba(34, 160, 107, 0.15);
}

.dot-check {
  font-size: 18rpx;
  color: #ffffff;
}

.dot-num {
  font-size: 18rpx;
  color: #9aa4b2;
}

.dot.active .dot-num {
  color: #ffffff;
}

.line {
  flex: 1;
  width: 2rpx;
  margin: 6rpx 0;
  min-height: 60rpx;
  background-color: #e2e8f0;
}

.line.done {
  background-color: #9fd9bd;
}

.timeline-main {
  flex: 1;
  padding: 0 0 28rpx 16rpx;
}

.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.timeline-title {
  font-size: 26rpx;
  color: #4b5563;
}

.timeline-title.active {
  color: #15803d;
  font-weight: 600;
}

.timeline-time {
  font-size: 21rpx;
  color: #9aa4b2;
}

.timeline-desc {
  display: block;
  margin-top: 6rpx;
  padding: 14rpx 16rpx;
  border-radius: 10rpx;
  background-color: #f5f9ff;
  font-size: 23rpx;
  line-height: 1.6;
  color: #5b6472;
}

.tip-box {
  display: flex;
  margin-bottom: 24rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background-color: #eef8f2;
}

.tip-icon {
  flex-shrink: 0;
  width: 30rpx;
  height: 30rpx;
  margin-right: 10rpx;
  border-radius: 50%;
  background-color: #22a06b;
  color: #ffffff;
  font-size: 20rpx;
  line-height: 30rpx;
  text-align: center;
}

.tip-text {
  font-size: 23rpx;
  line-height: 1.65;
  color: #3f7a5e;
}

.rate-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.rated {
  font-size: 24rpx;
  color: #16a34a;
}

.rate-link {
  font-size: 24rpx;
  color: #1677ff;
}

.stars {
  display: flex;
  margin-top: 16rpx;
}

.star {
  margin-right: 10rpx;
  font-size: 40rpx;
  color: #d7dde7;
}

.star.active {
  color: #f59e0b;
}

.rate-hint {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  color: #8b95a4;
}

.action-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background-color: #ffffff;
  box-shadow: 0 -6rpx 20rpx rgba(31, 45, 61, 0.06);
}

.action-btn {
  flex: 1;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 42rpx;
  text-align: center;
  font-size: 27rpx;
}

.action-btn.remind {
  margin-right: 20rpx;
  color: #22a06b;
  background-color: #ffffff;
  border: 2rpx solid #9fd9bd;
}

.action-btn.back {
  color: #ffffff;
  background: linear-gradient(135deg, #2fb47a 0%, #22a06b 100%);
}

.empty {
  padding: 160rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #9aa4b2;
}

.rating-mask {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(15, 23, 42, 0.45);
}

.rating-modal {
  width: 620rpx;
  padding: 36rpx 32rpx;
  border-radius: 24rpx;
  background-color: #ffffff;
}

.rating-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.rating-complaint {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #8b95a4;
}

.rate-stars {
  justify-content: center;
  margin: 28rpx 0 20rpx;
}

.rating-textarea {
  width: 100%;
  height: 160rpx;
  padding: 18rpx 20rpx;
  box-sizing: border-box;
  font-size: 25rpx;
  line-height: 1.6;
  background-color: #f5f7fa;
  border: 2rpx solid #e4e8ef;
  border-radius: 12rpx;
}

.rating-actions {
  display: flex;
  margin-top: 28rpx;
}

.rating-cancel {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 40rpx;
  text-align: center;
  font-size: 26rpx;
  color: #5b6472;
  background-color: #f5f7fa;
}

.rating-submit {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  margin-left: 24rpx;
  border-radius: 40rpx;
  text-align: center;
  font-size: 26rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #2fb47a 0%, #22a06b 100%);
}
</style>
