<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { fetchCreateCommunity, fetchDeleteCommunity, fetchGetCommunityList, fetchUpdateCommunity } from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
];

const loading = ref(false);
const keyword = ref('');
const statusFilter = ref<number | null>(null);
const communities = ref<Api.Business.Community[]>([]);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getCommunities();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getCommunities();
  }
});

const columns = computed<DataTableColumns<Api.Business.Community>>(() => [
  { key: 'name', title: '小区名称', minWidth: 160, ellipsis: { tooltip: true } },
  { key: 'address', title: '地址', minWidth: 200, ellipsis: { tooltip: true }, render: row => row.address || '-' },
  { key: 'builtYear', title: '建成年份', width: 90, align: 'center', render: row => row.builtYear || '-' },
  { key: 'buildingCount', title: '楼栋数', width: 80, align: 'center' },
  { key: 'householdCount', title: '户数', width: 80, align: 'center' },
  {
    key: 'manager',
    title: '负责人',
    width: 150,
    render: row => `${row.manager || '-'}${row.managerMobile ? ` ${row.managerMobile}` : ''}`
  },
  {
    key: 'status',
    title: '状态',
    width: 90,
    align: 'center',
    render: row =>
      h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small', bordered: false }, () =>
        row.status === 1 ? '启用' : '停用'
      )
  },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 140,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('community:edit')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) }, () =>
              $t('common.edit')
            )
          );
        }

        if (hasAuth('community:delete')) {
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

async function getCommunities() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetCommunityList({
      keyword: keyword.value || undefined,
      status: (statusFilter.value ?? undefined) as 0 | 1 | undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    communities.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  getCommunities();
}

function handleReset() {
  keyword.value = '';
  statusFilter.value = null;
  pagination.page = 1;
  getCommunities();
}

async function handleDelete(row: Api.Business.Community) {
  const { error } = await fetchDeleteCommunity(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getCommunities();
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);

const model = reactive({
  name: '',
  address: '',
  builtYear: null as number | null,
  buildingCount: 0,
  householdCount: 0,
  manager: '',
  managerMobile: '',
  status: 1 as 0 | 1,
  remark: ''
});

const formRules: FormRules = {
  name: { required: true, message: '请输入小区名称', trigger: ['blur', 'input'] }
};

const modalTitle = computed(() => (editingId.value ? '编辑小区' : '新增小区'));

function resetModel() {
  model.name = '';
  model.address = '';
  model.builtYear = null;
  model.buildingCount = 0;
  model.householdCount = 0;
  model.manager = '';
  model.managerMobile = '';
  model.status = 1;
  model.remark = '';
}

function handleAdd() {
  editingId.value = null;
  resetModel();
  modalVisible.value = true;
}

function handleEdit(row: Api.Business.Community) {
  editingId.value = row.id;
  model.name = row.name;
  model.address = row.address;
  model.builtYear = row.builtYear;
  model.buildingCount = row.buildingCount;
  model.householdCount = row.householdCount;
  model.manager = row.manager;
  model.managerMobile = row.managerMobile;
  model.status = row.status;
  model.remark = row.remark;
  modalVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();

  const payload: Api.Business.CommunitySaveParams = {
    name: model.name,
    address: model.address,
    builtYear: model.builtYear,
    buildingCount: model.buildingCount,
    householdCount: model.householdCount,
    manager: model.manager,
    managerMobile: model.managerMobile,
    status: model.status,
    remark: model.remark
  };

  submitLoading.value = true;

  try {
    const { error } = editingId.value
      ? await fetchUpdateCommunity(editingId.value, payload)
      : await fetchCreateCommunity(payload);

    if (error) return;

    window.$message?.success(editingId.value ? $t('common.modifySuccess') : $t('common.addSuccess'));
    modalVisible.value = false;
    await getCommunities();
  } finally {
    submitLoading.value = false;
  }
}

onMounted(getCommunities);
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
              placeholder="搜索小区名称 / 地址 / 负责人"
              class="w-260px"
              @keyup.enter="handleSearch"
            />
            <NSelect
              v-model:value="statusFilter"
              :options="statusOptions"
              clearable
              placeholder="状态"
              class="w-120px"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('community:add')" type="primary" @click="handleAdd">
            {{ $t('common.add') }}
          </NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="communities"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1000"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 620px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="小区名称" path="name">
          <NInput v-model:value="model.name" placeholder="请输入小区名称" />
        </NFormItem>
        <NFormItem label="地址">
          <NInput v-model:value="model.address" placeholder="请输入小区地址" />
        </NFormItem>
        <NGrid :cols="2" :x-gap="16">
          <NGi>
            <NFormItem label="建成年份">
              <NInputNumber v-model:value="model.builtYear" :min="1900" :max="2100" class="w-full" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="楼栋数">
              <NInputNumber v-model:value="model.buildingCount" :min="0" class="w-full" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="户数">
              <NInputNumber v-model:value="model.householdCount" :min="0" class="w-full" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="状态">
              <NSwitch v-model:value="model.status" :checked-value="1" :unchecked-value="0" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="负责人">
              <NInput v-model:value="model.manager" placeholder="请输入负责人" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="负责人电话">
              <NInput v-model:value="model.managerMobile" placeholder="请输入联系电话" />
            </NFormItem>
          </NGi>
        </NGrid>
        <NFormItem label="备注">
          <NInput v-model:value="model.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped></style>
