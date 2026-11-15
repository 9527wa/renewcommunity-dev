const ROLE_NAME_MAP: Record<string, string> = {
  R_SUPER: '超级管理员',
  R_ADMIN: '项目管理员',
  R_USER: '普通用户'
};

/** 将后端角色编码转换为展示名称 */
export function getRoleText(roles: string[]): string {
  if (!roles.length) {
    return '普通用户';
  }

  return roles.map(role => ROLE_NAME_MAP[role] ?? role).join('、');
}
