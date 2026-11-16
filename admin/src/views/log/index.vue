<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NTag } from 'naive-ui';
import { fetchGetOperationLogList } from '@/service/api';
import { $t } from '@/locales';

const loading = ref(false);
const keyword = ref('');
const successFilter = ref<number | null>(null);
const logs = ref<Api.Business.OperationLog[]>([]);

const successOptions = [
  { label: '成功', value: 1 },
  { label: '失败', value: 0 }
];

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getLogs();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getLogs();
  }
});

function formatDateTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-';
}

const methodTypeMap: Record<string, 'success' | 'info' | 'warning' | 'error' | 'default'> = {
  POST: 'info',
  PUT: 'warning',
  PATCH: 'warning',
  DELETE: 'error'
};

const columns = computed<DataTableColumns<Api.Business.OperationLog>>(() => [
  { key: 'userName', title: '操作人', width: 120, render: row => row.userName || '-' },
  {
    key: 'method',
    title: '方法',
    width: 90,
    align: 'center',
    render: row =>
      h(NTag, { type: methodTypeMap[row.method] || 'default', size: 'small', bordered: false }, () => row.method)
  },
  { key: 'path', title: '请求路径', minWidth: 240, ellipsis: { tooltip: true } },
  { key: 'ip', title: 'IP', width: 130, render: row => row.ip || '-' },
  { key: 'durationMs', title: '耗时', width: 100, align: 'right', render: row => `${row.durationMs} ms` },
  {
    key: 'success',
    title: '结果',
    width: 80,
    align: 'center',
    render: row =>
      h(NTag, { type: row.success === 1 ? 'success' : 'error', size: 'small', bordered: false }, () =>
        row.success === 1 ? '成功' : '失败'
      )
  },
  {
    key: 'errorMsg',
    title: '失败信息',
    minWidth: 160,
    ellipsis: { tooltip: true },
    render: row => row.errorMsg || '-'
  },
  { key: 'createdAt', title: '操作时间', width: 170, render: row => formatDateTime(row.createdAt) }
]);

async function getLogs() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetOperationLogList({
      keyword: keyword.value || undefined,
      success: (successFilter.value ?? undefined) as 0 | 1 | undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    logs.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  getLogs();
}

function handleReset() {
  keyword.value = '';
  successFilter.value = null;
  pagination.page = 1;
  getLogs();
}

onMounted(getLogs);
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace align="center" :size="12">
          <NInput
            v-model:value="keyword"
            clearable
            placeholder="搜索操作人 / 路径 / IP"
            class="w-260px"
            @keyup.enter="handleSearch"
          />
          <NSelect
            v-model:value="successFilter"
            :options="successOptions"
            clearable
            placeholder="执行结果"
            class="w-130px"
          />
          <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
          <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="logs"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1200"
        />
      </NSpace>
    </NCard>
  </div>
</template>

<style scoped></style>
