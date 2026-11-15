<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, UploadFileInfo } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import {
  downloadDocument,
  fetchDeleteDocument,
  fetchGetDocumentList,
  fetchGetProjectOptions,
  fetchUpdateDocumentStatus,
  fetchUploadDocument
} from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const statusMap: Record<number, { label: string; type: 'success' | 'info' }> = {
  0: { label: '正常', type: 'success' },
  1: { label: '已归档', type: 'info' }
};

const statusOptions = Object.entries(statusMap).map(([value, item]) => ({
  label: item.label,
  value: Number(value)
}));

const categories = ['立项资料', '设计图纸', '施工资料', '验收资料', '现场照片', '其他'];
const categoryOptions = categories.map(item => ({ label: item, value: item }));

const loading = ref(false);
const keyword = ref('');
const categoryFilter = ref<string | null>(null);
const projectFilter = ref<number | null>(null);
const statusFilter = ref<number | null>(null);
const documents = ref<Api.Business.Document[]>([]);
const projects = ref<Api.Business.Project[]>([]);

const projectOptions = computed(() => projects.value.map(item => ({ label: item.name, value: item.id })));

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getDocuments();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getDocuments();
  }
});

function formatDateTime(value?: string | null) {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 16);
}

function formatSize(value?: number | null) {
  if (value === undefined || value === null) {
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

const columns = computed<DataTableColumns<Api.Business.Document>>(() => [
  { key: 'title', title: '资料名称', minWidth: 160, ellipsis: { tooltip: true } },
  { key: 'category', title: '分类', width: 90 },
  {
    key: 'projectName',
    title: '关联项目',
    width: 140,
    ellipsis: { tooltip: true },
    render: row => row.projectName || '-'
  },
  {
    key: 'file',
    title: '文件',
    width: 190,
    render: row =>
      h('div', {}, [
        h('div', { style: 'font-size: 13px; line-height: 1.4;' }, row.fileName || '-'),
        h('div', { style: 'font-size: 11px; color: #9aa4b2; margin-top: 2px;' }, formatSize(row.fileSize))
      ])
  },
  { key: 'uploaderName', title: '上传人', width: 100, render: row => row.uploaderName || '-' },
  { key: 'createdAt', title: '上传时间', width: 140, render: row => formatDateTime(row.createdAt) },
  {
    key: 'status',
    title: '状态',
    width: 80,
    align: 'center',
    render: row => {
      const status = statusMap[row.status];

      return h(NTag, { type: status.type, size: 'small', bordered: false }, () => status.label);
    }
  },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 170,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('document:download')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'info', onClick: () => handleDownload(row) }, () => '下载')
          );
        }

        if (hasAuth('document:archive')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleToggleArchive(row) }, () =>
              row.status === 1 ? '取消归档' : '归档'
            )
          );
        }

        if (hasAuth('document:delete')) {
          buttons.push(
            h(
              NPopconfirm,
              { onPositiveClick: () => handleDelete(row) },
              {
                trigger: () => h(NButton, { size: 'small', text: true, type: 'error' }, () => $t('common.delete')),
                default: () => $t('common.confirmDelete')
              }
            )
          );
        }

        return buttons;
      })
  }
]);

async function getDocuments() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetDocumentList({
      keyword: keyword.value || undefined,
      category: categoryFilter.value ?? undefined,
      projectId: projectFilter.value ?? undefined,
      status: (statusFilter.value ?? undefined) as Api.Business.DocumentStatus | undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    documents.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

async function getProjects() {
  const { data, error } = await fetchGetProjectOptions();

  if (!error) {
    projects.value = data;
  }
}

function handleSearch() {
  pagination.page = 1;
  getDocuments();
}

function handleReset() {
  keyword.value = '';
  categoryFilter.value = null;
  projectFilter.value = null;
  statusFilter.value = null;
  pagination.page = 1;
  getDocuments();
}

async function handleDownload(row: Api.Business.Document) {
  try {
    await downloadDocument(row.id, row.fileName);
  } catch {
    window.$message?.error('文件下载失败');
  }
}

async function handleToggleArchive(row: Api.Business.Document) {
  const target = row.status === 1 ? 0 : 1;
  const { error } = await fetchUpdateDocumentStatus(row.id, target as Api.Business.DocumentStatus);

  if (error) return;

  window.$message?.success(target === 1 ? '资料已归档' : '已取消归档');
  await getDocuments();
}

async function handleDelete(row: Api.Business.Document) {
  const { error } = await fetchDeleteDocument(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getDocuments();
}

const uploadVisible = ref(false);
const uploadLoading = ref(false);
const fileList = ref<UploadFileInfo[]>([]);

const uploadModel = reactive({
  projectId: null as number | null,
  category: '其他',
  title: ''
});

function openUpload() {
  uploadModel.projectId = null;
  uploadModel.category = '其他';
  uploadModel.title = '';
  fileList.value = [];
  uploadVisible.value = true;
}

async function handleUpload() {
  const selected = fileList.value[0]?.file;

  if (!selected) {
    window.$message?.warning('请选择要上传的文件');
    return;
  }

  const formData = new FormData();
  formData.append('file', selected, selected.name);

  if (uploadModel.projectId !== null) {
    formData.append('projectId', String(uploadModel.projectId));
  }

  formData.append('category', uploadModel.category);

  if (uploadModel.title.trim()) {
    formData.append('title', uploadModel.title.trim());
  }

  uploadLoading.value = true;

  try {
    const { error } = await fetchUploadDocument(formData);

    if (error) return;

    window.$message?.success('资料上传成功');
    uploadVisible.value = false;
    await getDocuments();
  } finally {
    uploadLoading.value = false;
  }
}

onMounted(() => {
  getDocuments();
  getProjects();
});
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace justify="space-between" align="center">
          <NSpace align="center" :size="12">
            <NInput
              v-model:value="keyword"
              clearable
              placeholder="搜索资料名称"
              class="w-200px"
              @keyup.enter="handleSearch"
            />
            <NSelect
              v-model:value="categoryFilter"
              :options="categoryOptions"
              clearable
              placeholder="资料分类"
              class="w-130px"
            />
            <NSelect
              v-model:value="projectFilter"
              :options="projectOptions"
              clearable
              filterable
              placeholder="关联项目"
              class="w-200px"
            />
            <NSelect
              v-model:value="statusFilter"
              :options="statusOptions"
              clearable
              placeholder="状态"
              class="w-110px"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('document:upload')" type="primary" @click="openUpload">上传资料</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="documents"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1080"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="uploadVisible" preset="card" title="上传资料" style="width: 620px">
      <NForm :model="uploadModel" label-placement="left" :label-width="90">
        <NFormItem label="关联项目">
          <NSelect
            v-model:value="uploadModel.projectId"
            :options="projectOptions"
            clearable
            filterable
            placeholder="可不选"
          />
        </NFormItem>
        <NFormItem label="资料分类">
          <NSelect v-model:value="uploadModel.category" :options="categoryOptions" />
        </NFormItem>
        <NFormItem label="资料名称">
          <NInput v-model:value="uploadModel.title" placeholder="不填则使用文件名" />
        </NFormItem>
        <NFormItem label="选择文件">
          <NSpace vertical :size="8" class="w-full">
            <NUpload v-model:file-list="fileList" :default-upload="false" :max="1" class="w-full">
              <NButton>选择文件</NButton>
            </NUpload>
            <NText depth="3" class="text-xs">单个文件不超过 50MB，支持各类文档、图片与压缩包</NText>
          </NSpace>
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="uploadVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="uploadLoading" @click="handleUpload">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped></style>
