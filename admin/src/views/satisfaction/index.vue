<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NProgress, NSpace } from 'naive-ui';
import { fetchGetSatisfactionList, fetchGetSatisfactionStats } from '@/service/api';
import { $t } from '@/locales';

const scoreOptions = [5, 4, 3, 2, 1].map(score => ({ label: `${score} 星`, value: score }));

const loading = ref(false);
const scoreFilter = ref<number | null>(null);
const records = ref<Api.Business.Satisfaction[]>([]);
const stats = ref<Api.Business.SatisfactionStats>({
  total: 0,
  averageScore: 0,
  fiveCount: 0,
  fourCount: 0,
  goodRate: 0,
  oneCount: 0,
  twoCount: 0,
  threeCount: 0
});

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getRecords();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getRecords();
  }
});

const distribution = computed(() => {
  const total = stats.value.total || 1;
  const map: Record<number, number> = {
    5: stats.value.fiveCount,
    4: stats.value.fourCount,
    3: stats.value.threeCount,
    2: stats.value.twoCount,
    1: stats.value.oneCount
  };

  return [5, 4, 3, 2, 1].map(score => ({
    score,
    count: map[score],
    percentage: Math.round((map[score] * 1000) / total) / 10
  }));
});

const columns = computed<DataTableColumns<Api.Business.Satisfaction>>(() => [
  {
    key: 'complaintTitle',
    title: '投诉标题',
    minWidth: 200,
    ellipsis: { tooltip: true },
    render: row => row.complaintTitle || '-'
  },
  { key: 'userName', title: '居民', width: 120, render: row => row.userName || '-' },
  {
    key: 'score',
    title: '评分',
    width: 150,
    render: row =>
      h(
        'span',
        { style: 'color: #f59e0b; letter-spacing: 2px; font-size: 16px;' },
        '★'.repeat(row.score) + '☆'.repeat(Math.max(5 - row.score, 0))
      )
  },
  { key: 'comment', title: '评价内容', minWidth: 240, ellipsis: { tooltip: true }, render: row => row.comment || '-' },
  {
    key: 'createdAt',
    title: '评价时间',
    width: 160,
    render: row => row.createdAt.replace('T', ' ').slice(0, 16)
  }
]);

async function getRecords() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetSatisfactionList({
      score: scoreFilter.value ?? undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    records.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

async function getStats() {
  const { data, error } = await fetchGetSatisfactionStats();

  if (!error) {
    stats.value = data;
  }
}

function handleSearch() {
  pagination.page = 1;
  getRecords();
}

function handleReset() {
  scoreFilter.value = null;
  pagination.page = 1;
  getRecords();
}

onMounted(() => {
  getRecords();
  getStats();
});
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NGrid :cols="3" :x-gap="16" :y-gap="16">
          <NGi><NStatistic label="评价总数" :value="stats.total" /></NGi>
          <NGi><NStatistic label="平均评分" :value="stats.averageScore.toFixed(1)" /></NGi>
          <NGi><NStatistic label="好评率（4 星及以上）" :value="`${stats.goodRate}%`" /></NGi>
        </NGrid>

        <NSpace vertical :size="10">
          <view v-for="item in distribution" :key="item.score" class="distribution-row">
            <text class="score-label">{{ item.score }} 星</text>
            <NProgress
              type="line"
              :percentage="item.percentage"
              :show-indicator="false"
              :height="8"
              class="distribution-bar"
            />
            <text class="score-count">{{ item.count }}</text>
          </view>
        </NSpace>

        <NSpace align="center" :size="12">
          <NSelect
            v-model:value="scoreFilter"
            :options="scoreOptions"
            clearable
            placeholder="评分筛选"
            class="w-130px"
          />
          <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
          <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="records"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="950"
        />
      </NSpace>
    </NCard>
  </div>
</template>

<style scoped>
.distribution-row {
  display: flex;
  align-items: center;
}

.score-label {
  width: 52px;
  font-size: 13px;
  color: #6b7280;
}

.distribution-bar {
  flex: 1;
  max-width: 420px;
}

.score-count {
  width: 48px;
  margin-left: 12px;
  text-align: right;
  font-size: 13px;
  color: #4b5563;
}
</style>
