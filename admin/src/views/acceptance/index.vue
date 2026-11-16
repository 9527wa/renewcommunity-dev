<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules, UploadFileInfo } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import {
  downloadSystemFile,
  fetchCreateAcceptance,
  fetchDeleteAcceptance,
  fetchGetAcceptanceList,
  fetchGetProjectOptions,
  fetchUpdateAcceptance
} from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const resultMap: Record<number, { label: string; type: 'default' | 'success' | 'error' }> = {
  0: { label: '待验收', type: 'default' },
  1: { label: '通过', type: 'success' },
  2: { label: '不通过', type: 'error' }
};

const resultOptions = Object.entries(resultMap).map(([value, item]) => ({
  label: item.label,
  value: Number(value)
}));

const loading = ref(false);
const projectFilter = ref<number | null>(null);
const resultFilter = ref<number | null>(null);
const records = ref<Api.Business.Acceptance[]>([]);
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
    getRecords();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getRecords();
  }
});

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

const columns = computed<DataTableColumns<Api.Business.Acceptance>>(() => [
  { key: 'projectName', title: '项目', width: 160, ellipsis: { tooltip: true }, render: row => row.projectName || '-' },
  { key: 'title', title: '验收名称', minWidth: 180, ellipsis: { tooltip: true } },
  {
    key: 'result',
    title: '验收结果',
    width: 100,
    align: 'center',
    render: row => {
      const result = resultMap[row.result];

      return h(NTag, { type: result.type, size: 'small', bordered: false }, () => result.label);
    }
  },
  { key: 'acceptDate', title: '验收日期', width: 110, render: row => row.acceptDate || '-' },
  { key: 'creatorName', title: '登记人', width: 100, render: row => row.creatorName || '-' },
  {
    key: 'attachments',
    title: '验收附件',
    width: 110,
    align: 'center',
    render: row =>
      row.attachments.length
        ? h(
            NButton,
            { size: 'small', text: true, type: 'info', onClick: () => openAttachments(row) },
            () => `查看 (${row.attachments.length})`
          )
        : '-'
  },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 150,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('acceptance:edit')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) }, () =>
              $t('common.edit')
            )
          );
        }

        if (hasAuth('acceptance:delete')) {
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

async function getRecords() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetAcceptanceList({
      projectId: projectFilter.value ?? undefined,
      result: (resultFilter.value ?? undefined) as Api.Business.AcceptanceResult | undefined,
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

async function getProjects() {
  const { data, error } = await fetchGetProjectOptions();

  if (!error) {
    projects.value = data;
  }
}

function handleSearch() {
  pagination.page = 1;
  getRecords();
}

function handleReset() {
  projectFilter.value = null;
  resultFilter.value = null;
  pagination.page = 1;
  getRecords();
}

async function handleDelete(row: Api.Business.Acceptance) {
  const { error } = await fetchDeleteAcceptance(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getRecords();
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);
const fileList = ref<UploadFileInfo[]>([]);

const model = reactive({
  projectId: null as number | null,
  title: '',
  result: 0 as Api.Business.AcceptanceResult,
  acceptDate: today(),
  conclusion: ''
});

const formRules: FormRules = {
  projectId: { required: true, type: 'number', message: '请选择项目', trigger: ['blur', 'change'] },
  title: { required: true, message: '请输入验收名称', trigger: ['blur', 'input'] }
};

const modalTitle = computed(() => (editingId.value ? '编辑验收记录' : '登记验收'));

function resetModel() {
  model.projectId = null;
  model.title = '';
  model.result = 0;
  model.acceptDate = today();
  model.conclusion = '';
  fileList.value = [];
}

function handleAdd() {
  editingId.value = null;
  resetModel();
  modalVisible.value = true;
}

function handleEdit(row: Api.Business.Acceptance) {
  editingId.value = row.id;
  model.projectId = row.projectId;
  model.title = row.title;
  model.result = row.result;
  model.acceptDate = row.acceptDate || '';
  model.conclusion = row.conclusion;
  fileList.value = [];
  modalVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();

  submitLoading.value = true;

  try {
    if (editingId.value) {
      const { error } = await fetchUpdateAcceptance(editingId.value, {
        title: model.title,
        result: model.result,
        acceptDate: model.acceptDate || null,
        conclusion: model.conclusion
      });

      if (error) return;
    } else {
      const formData = new FormData();
      formData.append('projectId', String(model.projectId));
      formData.append('title', model.title);
      formData.append('result', String(model.result));
      formData.append('conclusion', model.conclusion);

      if (model.acceptDate) {
        formData.append('acceptDate', model.acceptDate);
      }

      fileList.value.forEach(item => {
        if (item.file) {
          formData.append('files', item.file, item.file.name);
        }
      });

      const { error } = await fetchCreateAcceptance(formData);

      if (error) return;
    }

    window.$message?.success(editingId.value ? $t('common.modifySuccess') : '验收登记成功');
    modalVisible.value = false;
    await getRecords();
  } finally {
    submitLoading.value = false;
  }
}

const attachmentsVisible = ref(false);
const activeAttachments = ref<Api.Business.AcceptanceFile[]>([]);
const activeTitle = ref('');

function openAttachments(row: Api.Business.Acceptance) {
  activeAttachments.value = row.attachments;
  activeTitle.value = row.title;
  attachmentsVisible.value = true;
}

async function handleDownloadAttachment(file: Api.Business.AcceptanceFile) {
  try {
    await downloadSystemFile(file.fileId, file.fileName);
  } catch {
    window.$message?.error('附件下载失败');
  }
}

onMounted(() => {
  getRecords();
  getProjects();
});
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace justify="space-between" align="center">
          <NSpace align="center" :size="12">
            <NSelect
              v-model:value="projectFilter"
              :options="projectOptions"
              clearable
              filterable
              placeholder="选择项目"
              class="w-220px"
            />
            <NSelect
              v-model:value="resultFilter"
              :options="resultOptions"
              clearable
              placeholder="验收结果"
              class="w-130px"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('acceptance:add')" type="primary" @click="handleAdd">登记验收</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="records"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1050"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 680px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="所属项目" path="projectId">
          <NSelect
            v-model:value="model.projectId"
            :options="projectOptions"
            :disabled="Boolean(editingId)"
            filterable
            placeholder="请选择项目"
          />
        </NFormItem>
        <NFormItem label="验收名称" path="title">
          <NInput v-model:value="model.title" placeholder="如 绿化提升工程竣工验收" />
        </NFormItem>
        <NGrid :cols="2" :x-gap="16">
          <NGi>
            <NFormItem label="验收结果">
              <NSelect v-model:value="model.result" :options="resultOptions" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="验收日期">
              <NDatePicker
                v-model:formatted-value="model.acceptDate"
                value-format="yyyy-MM-dd"
                type="date"
                clearable
                class="w-full"
              />
            </NFormItem>
          </NGi>
        </NGrid>
        <NFormItem label="验收结论">
          <NInput v-model:value="model.conclusion" type="textarea" :rows="3" placeholder="请输入验收结论" />
        </NFormItem>
        <NFormItem v-if="!editingId" label="验收附件">
          <NSpace vertical :size="8" class="w-full">
            <NUpload v-model:file-list="fileList" :max="5" :default-upload="false" class="w-full">
              <NButton>选择附件</NButton>
            </NUpload>
            <NText depth="3" class="text-xs">最多上传 5 个附件，单个不超过 50MB</NText>
          </NSpace>
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal v-model:show="attachmentsVisible" preset="card" :title="`验收附件 · ${activeTitle}`" style="width: 560px">
      <NSpace vertical :size="12">
        <NText v-if="!activeAttachments.length" depth="3" class="text-sm">暂无附件</NText>
        <NSpace v-for="file in activeAttachments" :key="file.id" justify="space-between" align="center" class="py-6px">
          <NSpace vertical :size="2">
            <NText class="text-sm">{{ file.fileName || '-' }}</NText>
            <NText depth="3" class="text-xs">{{ formatSize(file.fileSize) }}</NText>
          </NSpace>
          <NButton size="small" type="primary" text @click="handleDownloadAttachment(file)">下载</NButton>
        </NSpace>
      </NSpace>
    </NModal>
  </div>
</template>

<style scoped></style>
