<script setup lang="ts">
import { computed, h, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules, UploadFileInfo } from 'naive-ui';
import { NButton, NImage, NPopconfirm, NProgress, NSpace } from 'naive-ui';
import {
  fetchCreateProgress,
  fetchDeleteProgress,
  fetchFileObjectUrl,
  fetchGetProgressList,
  fetchGetProjectOptions,
  fetchUpdateProgress
} from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const stageOptions = ['准备阶段', '施工阶段', '验收阶段'].map(item => ({ label: item, value: item }));

const loading = ref(false);
const projectFilter = ref<number | null>(null);
const stageFilter = ref<string | null>(null);
const records = ref<Api.Business.Progress[]>([]);
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

const columns = computed<DataTableColumns<Api.Business.Progress>>(() => [
  { key: 'projectName', title: '项目', width: 160, ellipsis: { tooltip: true }, render: row => row.projectName || '-' },
  { key: 'stage', title: '阶段', width: 100 },
  { key: 'title', title: '进度标题', minWidth: 180, ellipsis: { tooltip: true } },
  {
    key: 'progress',
    title: '进度',
    width: 160,
    render: row =>
      h(NSpace, { align: 'center', size: 8, wrap: false }, () => [
        h(NProgress, { type: 'line', percentage: row.progress, height: 8, showIndicator: false, style: 'width: 90px' }),
        h('span', { style: 'width: 38px; text-align: right; font-size: 12px; color: #4b5563;' }, `${row.progress}%`)
      ])
  },
  { key: 'reportDate', title: '上报日期', width: 110 },
  { key: 'creatorName', title: '上报人', width: 100, render: row => row.creatorName || '-' },
  {
    key: 'images',
    title: '现场图片',
    width: 110,
    align: 'center',
    render: row =>
      row.images.length
        ? h(
            NButton,
            { size: 'small', text: true, type: 'info', onClick: () => openImages(row) },
            () => `查看 (${row.images.length})`
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

        if (hasAuth('progress:edit')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) }, () =>
              $t('common.edit')
            )
          );
        }

        if (hasAuth('progress:delete')) {
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
    const { data, error } = await fetchGetProgressList({
      projectId: projectFilter.value ?? undefined,
      stage: stageFilter.value ?? undefined,
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
  stageFilter.value = null;
  pagination.page = 1;
  getRecords();
}

async function handleDelete(row: Api.Business.Progress) {
  const { error } = await fetchDeleteProgress(row.id);

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
  stage: '施工阶段',
  title: '',
  content: '',
  progress: 0,
  reportDate: today()
});

const formRules: FormRules = {
  projectId: { required: true, type: 'number', message: '请选择项目', trigger: ['blur', 'change'] },
  title: { required: true, message: '请输入进度标题', trigger: ['blur', 'input'] },
  reportDate: { required: true, message: '请选择上报日期', trigger: ['blur', 'change'] }
};

const modalTitle = computed(() => (editingId.value ? '编辑进度' : '上报进度'));

function resetModel() {
  model.projectId = null;
  model.stage = '施工阶段';
  model.title = '';
  model.content = '';
  model.progress = 0;
  model.reportDate = today();
  fileList.value = [];
}

function handleAdd() {
  editingId.value = null;
  resetModel();
  modalVisible.value = true;
}

function handleEdit(row: Api.Business.Progress) {
  editingId.value = row.id;
  model.projectId = row.projectId;
  model.stage = row.stage;
  model.title = row.title;
  model.content = row.content;
  model.progress = row.progress;
  model.reportDate = row.reportDate;
  fileList.value = [];
  modalVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();

  submitLoading.value = true;

  try {
    if (editingId.value) {
      const { error } = await fetchUpdateProgress(editingId.value, {
        stage: model.stage,
        title: model.title,
        content: model.content,
        progress: model.progress,
        reportDate: model.reportDate
      });

      if (error) return;
    } else {
      const formData = new FormData();
      formData.append('projectId', String(model.projectId));
      formData.append('stage', model.stage);
      formData.append('title', model.title);
      formData.append('content', model.content);
      formData.append('progress', String(model.progress));
      formData.append('reportDate', model.reportDate);

      fileList.value.forEach(item => {
        if (item.file) {
          formData.append('files', item.file, item.file.name);
        }
      });

      const { error } = await fetchCreateProgress(formData);

      if (error) return;
    }

    window.$message?.success(editingId.value ? $t('common.modifySuccess') : '进度上报成功');
    modalVisible.value = false;
    await getRecords();
  } finally {
    submitLoading.value = false;
  }
}

const imageModalVisible = ref(false);
const previewImages = ref<Array<{ fileId: number; url: string; name: string }>>([]);

async function openImages(row: Api.Business.Progress) {
  try {
    const images = await Promise.all(
      row.images.map(async image => ({
        fileId: image.fileId,
        url: await fetchFileObjectUrl(image.fileId),
        name: image.fileName || `图片${image.fileId}`
      }))
    );

    previewImages.value = images;
    imageModalVisible.value = true;
  } catch {
    window.$message?.error('图片加载失败');
  }
}

function closeImageModal() {
  previewImages.value.forEach(image => window.URL.revokeObjectURL(image.url));
  previewImages.value = [];
}

onBeforeUnmount(closeImageModal);

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
              v-model:value="stageFilter"
              :options="stageOptions"
              clearable
              placeholder="施工阶段"
              class="w-130px"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('progress:add')" type="primary" @click="handleAdd">上报进度</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="records"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1100"
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
        <NFormItem label="施工阶段">
          <NSelect v-model:value="model.stage" :options="stageOptions" />
        </NFormItem>
        <NFormItem label="进度标题" path="title">
          <NInput v-model:value="model.title" placeholder="如 道路基层处理完成" />
        </NFormItem>
        <NFormItem label="进度说明">
          <NInput v-model:value="model.content" type="textarea" :rows="3" placeholder="请输入进度说明" />
        </NFormItem>
        <NGrid :cols="2" :x-gap="16">
          <NGi>
            <NFormItem label="更新后进度">
              <NInputNumber v-model:value="model.progress" :min="0" :max="100" class="w-full" suffix="%" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="上报日期" path="reportDate">
              <NDatePicker
                v-model:formatted-value="model.reportDate"
                value-format="yyyy-MM-dd"
                type="date"
                class="w-full"
              />
            </NFormItem>
          </NGi>
        </NGrid>
        <NFormItem v-if="!editingId" label="现场图片">
          <NSpace vertical :size="8" class="w-full">
            <NUpload
              v-model:file-list="fileList"
              list-type="image-card"
              accept="image/*"
              :max="5"
              :default-upload="false"
            />
            <NText depth="3" class="text-xs">最多上传 5 张现场图片，单张不超过 50MB</NText>
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

    <NModal
      v-model:show="imageModalVisible"
      preset="card"
      title="现场图片"
      style="width: 720px"
      @after-leave="closeImageModal"
    >
      <NSpace :size="12" class="flex-wrap">
        <NImage
          v-for="image in previewImages"
          :key="image.fileId"
          :src="image.url"
          :alt="image.name"
          width="180"
          object-fit="cover"
          class="rounded-8px"
        />
      </NSpace>
      <NText v-if="!previewImages.length" depth="3" class="text-sm">暂无图片</NText>
    </NModal>
  </div>
</template>

<style scoped></style>
