<script setup lang="ts">
import { computed, h, nextTick, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';
import {
  fetchCreateUser,
  fetchDeleteUser,
  fetchGetRoleList,
  fetchGetUserList,
  fetchResetUserPassword,
  fetchUpdateUser
} from '@/service/api';

const { hasAuth } = useAuth();

const loading = ref(false);
const keyword = ref('');
const users = ref<Api.SystemManage.User[]>([]);
const roles = ref<Api.SystemManage.Role[]>([]);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getUsers();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getUsers();
  }
});

const roleNameMap = computed(() => new Map(roles.value.map(role => [role.id, role.name])));

const roleOptions = computed(() => roles.value.map(role => ({ label: `${role.name} (${role.code})`, value: role.id })));

const columns = computed<DataTableColumns<Api.SystemManage.User>>(() => [
  { key: 'username', title: '用户名', width: 140, ellipsis: { tooltip: true } },
  { key: 'nickname', title: '昵称', width: 140, ellipsis: { tooltip: true } },
  {
    key: 'roleIds',
    title: '角色',
    minWidth: 160,
    render: row => {
      const names = row.roleIds.map(id => roleNameMap.value.get(id)).filter(Boolean);

      return names.length ? names.join('、') : '-';
    }
  },
  { key: 'mobile', title: '手机号', width: 140, render: row => row.mobile || '-' },
  { key: 'email', title: '邮箱', minWidth: 180, ellipsis: { tooltip: true }, render: row => row.email || '-' },
  {
    key: 'status',
    title: '状态',
    width: 90,
    align: 'center',
    render: row =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', size: 'small', bordered: false },
        () => (row.status === 1 ? '启用' : '停用')
      )
  },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 220,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('system:user:edit')) {
          buttons.push(
            h(
              NButton,
              { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) },
              () => $t('common.edit')
            )
          );
        }

        if (hasAuth('system:user:reset-password')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'warning', onClick: () => openReset(row) }, () => '重置密码')
          );
        }

        if (hasAuth('system:user:delete')) {
          buttons.push(
            h(NPopconfirm, { onPositiveClick: () => handleDelete(row) }, {
              trigger: () => h(NButton, { size: 'small', text: true, type: 'error' }, () => $t('common.delete')),
              default: () => $t('common.confirmDelete')
            })
          );
        }

        return buttons;
      })
  }
]);

async function getUsers() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetUserList({
      keyword: keyword.value.trim() || undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    users.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

async function getRoles() {
  const { data, error } = await fetchGetRoleList();

  if (!error) {
    roles.value = data;
  }
}

function handleSearch() {
  pagination.page = 1;
  getUsers();
}

function handleReset() {
  keyword.value = '';
  handleSearch();
}

async function handleDelete(row: Api.SystemManage.User) {
  const { error } = await fetchDeleteUser(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));

  if (users.value.length === 1 && pagination.page > 1) {
    pagination.page -= 1;
  }

  await getUsers();
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);

const model = reactive<Api.SystemManage.UserSaveParams>({
  username: '',
  password: '',
  nickname: '',
  mobile: '',
  email: '',
  status: 1,
  roleIds: []
});

const modalTitle = computed(() => (editingId.value ? '编辑用户' : '新增用户'));

const formRules = computed<FormRules>(() => {
  const rules: FormRules = {
    username: { required: true, message: '请输入用户名', trigger: ['input', 'blur'] },
    roleIds: { type: 'array', required: true, message: '请选择角色', trigger: ['change', 'blur'] }
  };

  if (!editingId.value) {
    rules.password = {
      required: true,
      min: 6,
      max: 32,
      message: '密码长度需在 6-32 位之间',
      trigger: ['input', 'blur']
    };
  }

  return rules;
});

function resetForm() {
  Object.assign(model, {
    username: '',
    password: '',
    nickname: '',
    mobile: '',
    email: '',
    status: 1,
    roleIds: []
  });
}

function handleAdd() {
  editingId.value = null;
  resetForm();
  modalVisible.value = true;
  nextTick(() => formRef.value?.restoreValidation());
}

function handleEdit(row: Api.SystemManage.User) {
  editingId.value = row.id;
  Object.assign(model, {
    username: row.username,
    password: '',
    nickname: row.nickname,
    mobile: row.mobile ?? '',
    email: row.email ?? '',
    status: row.status,
    roleIds: [...row.roleIds]
  });
  modalVisible.value = true;
  nextTick(() => formRef.value?.restoreValidation());
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  submitLoading.value = true;

  try {
    if (editingId.value) {
      const { error } = await fetchUpdateUser(editingId.value, {
        nickname: model.nickname,
        mobile: model.mobile || null,
        email: model.email || null,
        status: model.status,
        roleIds: model.roleIds
      });

      if (error) return;

      window.$message?.success($t('common.updateSuccess'));
    } else {
      const { error } = await fetchCreateUser({
        ...model,
        mobile: model.mobile || null,
        email: model.email || null
      });

      if (error) return;

      window.$message?.success($t('common.addSuccess'));
    }

    modalVisible.value = false;
    await getUsers();
  } finally {
    submitLoading.value = false;
  }
}

const resetVisible = ref(false);
const resetLoading = ref(false);
const resetFormRef = ref<FormInst | null>(null);
const resettingUser = ref<Api.SystemManage.User | null>(null);
const resetModel = reactive({ password: '' });

const resetRules: FormRules = {
  password: {
    required: true,
    min: 6,
    max: 32,
    message: '密码长度需在 6-32 位之间',
    trigger: ['input', 'blur']
  }
};

function openReset(row: Api.SystemManage.User) {
  resettingUser.value = row;
  resetModel.password = '';
  resetVisible.value = true;
  nextTick(() => resetFormRef.value?.restoreValidation());
}

async function handleResetPassword() {
  if (!resettingUser.value) return;

  try {
    await resetFormRef.value?.validate();
  } catch {
    return;
  }

  resetLoading.value = true;

  try {
    const { error } = await fetchResetUserPassword(resettingUser.value.id, resetModel.password);

    if (error) return;

    window.$message?.success('密码重置成功');
    resetVisible.value = false;
  } finally {
    resetLoading.value = false;
  }
}

onMounted(() => {
  getRoles();
  getUsers();
});
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace justify="space-between" align="center">
          <NSpace align="center">
            <NInput
              v-model:value="keyword"
              clearable
              placeholder="搜索用户名 / 昵称"
              class="w-240px"
              @keyup.enter="handleSearch"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('system:user:add')" type="primary" @click="handleAdd">
            {{ $t('common.add') }}
          </NButton>
        </NSpace>
        <NDataTable
          remote
          :columns="columns"
          :data="users"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1100"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 560px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="用户名" path="username">
          <NInput v-model:value="model.username" :disabled="Boolean(editingId)" placeholder="请输入用户名" />
        </NFormItem>
        <NFormItem v-if="!editingId" label="密码" path="password">
          <NInput v-model:value="model.password" type="password" show-password-on="click" placeholder="请输入密码" />
        </NFormItem>
        <NFormItem label="昵称" path="nickname">
          <NInput v-model:value="model.nickname" placeholder="请输入昵称" />
        </NFormItem>
        <NFormItem label="角色" path="roleIds">
          <NSelect v-model:value="model.roleIds" multiple :options="roleOptions" placeholder="请选择角色" />
        </NFormItem>
        <NFormItem label="手机号" path="mobile">
          <NInput v-model:value="model.mobile" placeholder="请输入手机号" />
        </NFormItem>
        <NFormItem label="邮箱" path="email">
          <NInput v-model:value="model.email" placeholder="请输入邮箱" />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NSwitch v-model:value="model.status" :checked-value="1" :unchecked-value="0" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal v-model:show="resetVisible" preset="card" title="重置密码" style="width: 420px">
      <NForm ref="resetFormRef" :model="resetModel" :rules="resetRules" label-placement="left" :label-width="90">
        <NFormItem label="用户名">
          <NInput :value="resettingUser?.username" disabled />
        </NFormItem>
        <NFormItem label="新密码" path="password">
          <NInput
            v-model:value="resetModel.password"
            type="password"
            show-password-on="click"
            placeholder="请输入新密码"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="resetVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="resetLoading" @click="handleResetPassword">
            {{ $t('common.confirm') }}
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped></style>
