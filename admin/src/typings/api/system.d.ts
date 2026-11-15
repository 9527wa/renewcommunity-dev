/**
 * Namespace Api
 *
 * Backend api type of the RBAC system module
 */
declare namespace Api {
  namespace SystemManage {
    /** enable status: 1 enabled, 0 disabled */
    type EnableStatus = 0 | 1;

    /** user list item */
    interface User {
      id: number;
      username: string;
      nickname: string;
      mobile: string | null;
      email: string | null;
      status: EnableStatus;
      roleIds: number[];
    }

    interface UserList {
      total: number;
      list: User[];
    }

    interface UserSearchParams {
      keyword?: string;
      pageNum: number;
      pageSize: number;
    }

    interface UserSaveParams {
      username: string;
      password: string;
      nickname?: string;
      mobile?: string | null;
      email?: string | null;
      status: EnableStatus;
      roleIds: number[];
    }

    interface UserUpdateParams {
      nickname?: string;
      mobile?: string | null;
      email?: string | null;
      status: EnableStatus;
      roleIds: number[];
    }

    /** role list item */
    interface Role {
      id: number;
      code: string;
      name: string;
      sort: number;
      status: EnableStatus;
      remark: string | null;
      menuIds: number[];
    }

    interface RoleSaveParams {
      code: string;
      name: string;
      sort: number;
      status: EnableStatus;
      remark?: string | null;
      menuIds?: number[];
    }

    /** menu tree node */
    interface Menu {
      id: number;
      parentId: number;
      name: string;
      type: MenuType;
      path: string;
      component: string;
      permission: string;
      icon: string;
      sort: number;
      status: EnableStatus;
      children: Menu[];
    }

    /** menu type: M directory, C menu, B button */
    type MenuType = 'M' | 'C' | 'B';

    interface MenuSaveParams {
      parentId: number;
      name: string;
      type: MenuType;
      path: string;
      component: string;
      permission: string;
      icon: string;
      sort: number;
      status: EnableStatus;
    }

    /** server status */
    interface ServerStatus {
      os: ServerOsInfo;
      memory: ServerMemoryInfo;
      jvm: ServerJvmInfo;
      app: ServerAppInfo;
    }

    interface ServerOsInfo {
      name: string;
      arch: string;
      version: string;
      availableProcessors: number;
      /** -1 means unavailable */
      systemLoadAverage: number;
      /** percentage 0-100, -1 means unavailable */
      cpuUsage: number;
    }

    /** all memory fields are bytes */
    interface ServerMemoryInfo {
      physicalTotal: number;
      physicalUsed: number;
      physicalFree: number;
      /** percentage 0-100 */
      physicalUsage: number;
      heapUsed: number;
      heapMax: number;
      heapCommitted: number;
      /** percentage 0-100 */
      heapUsage: number;
      nonHeapUsed: number;
      nonHeapCommitted: number;
    }

    interface ServerJvmInfo {
      javaVersion: string;
      javaVendor: string;
      jvmName: string;
      /** epoch millis */
      startTime: number;
      uptimeSeconds: number;
      threadCount: number;
      peakThreadCount: number;
      daemonThreadCount: number;
      loadedClassCount: number;
    }

    interface ServerAppInfo {
      name: string;
      /** comma separated active spring profiles */
      activeProfiles: string;
    }
  }
}
