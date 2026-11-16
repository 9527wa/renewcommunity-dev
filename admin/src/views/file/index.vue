<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NButton, NPopconfirm, NSpace } from 'naive-ui';
import { downloadSystemFile, fetchDeleteFileRecord, fetchGetFileList } from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const loading = ref(false);
const keyword = ref('');
const files = ref<Api.Business.FileItem[]>([]);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getFiles();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getFiles();
  }
});

function formatSize(value?: number) {
  if (!value) {
    return '-';
  }

  if (value < 1024) {
    return `${value} B`;
  }

  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(1)} KB`;
  }

  return `${(value / 1024 / 1024).toFixed(1)} MB`;
}

function formatDateTime(value?: string) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

const columns = computed<DataTableColumns<Api.Business.FileItem>>(() => [
  { key: 'originalName', title: '文件名', minWidth: 220, ellipsis: { tooltip: true } },
  { key: 'contentType', title: '类型', width: 150, render: row => row.contentType || '-' },
  { key: 'size', title: '大小', width: 100, align: 'right', render: row => formatSize(row.size) },
  { key: 'uploaderName', title: '上传人', width: 120, render: row => row.uploaderName || '-' },
  { key: 'createdAt', title: '上传时间', width: 160, render: row => formatDateTime(row.createdAt) },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 140,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('file:download')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'info', onClick: () => handleDownload(row) }, () => '下载')
          );
        }

        if (hasAuth('file:delete')) {
          buttons.push(
            h(
              NPopconfirm,
              { onPositiveClick: () => handleDelete(row) },
              {
                trigger: () => h(NButton, { size: 'small', text: true, type: 'error' }, () => $t('common.delete')),
                default: () => '文件被资料/进度/验收引用时无法删除'
              }
            )
          );
        }

        return buttons;
      })
  }
]);

async function getFiles() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetFileList({
      keyword: keyword.value || undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    files.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  getFiles();
}

function handleReset() {
  keyword.value = '';
  pagination.page = 1;
  getFiles();
}

async function handleDownload(row: Api.Business.FileItem) {
  try {
    await downloadSystemFile(row.id, row.originalName);
  } catch {
    window.$message?.error('文件下载失败');
  }
}

async function handleDelete(row: Api.Business.FileItem) {
  const { error } = await fetchDeleteFileRecord(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getFiles();
}

onMounted(getFiles);
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace align="center" :size="12">
          <NInput
            v-model:value="keyword"
            clearable
            placeholder="搜索文件名 / 上传人"
            class="w-260px"
            @keyup.enter="handleSearch"
          />
          <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
          <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="files"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="950"
        />
      </NSpace>
    </NCard>
  </div>
</template>

<style scoped></style>
